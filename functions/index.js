// Google Cloud Functions for Remind Me Later Firestore Event Triggers

/** WORK IN PROGRESS **/

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();


// Listen for writes to messages and call appropriate function
exports.messageListener = functions.firestore
    .document('messages/{messageId}').onCreate(async (snap, context) => {
        const messageId = context.params.messageId;
        const data = snap.data();
        const messageType = data.messageType;

        console.log('New message received.\n messageId: ' + messageId +
                    '\nmessageType: ' + messageType);
        console.log('data: %j', data);

        if (messageType == "friendRequest")
        {
            try {
                const receiverId = await lookupReceiverId(data.friendEmail);
                const receiverUserProfile = await getReceiverUserProfile(receiverId);
                //const receiverDeviceToken = receiverUserProfile.deviceToken;
                // check for missing token here?
                return sendFriendRequest(data, receiverUserProfile);
            }
            catch (error) {
                console.log('Error sending friend request: ', error);
            }
        }
        else
        {
            console.log('Other message type received: ', messageType);

            try {
                const receiverId = data.receiverId;
                const receiverUserProfile = await getReceiverUserProfile(receiverId);
                filterMessageType(data, receiverUserProfile);
            }
            catch (error) {
                console.log('Error handling request: ', error);
            }
        }

        /*
        try {
            const deleteMessage = await deleteMessage(messageId);
        }
        catch (error) {
            console.log('Error deleting message from cloud: ', error);
        }
        */

        return null;
    });


async function deleteMessage(messageId)
{
    let messageRef = db.collection('messages').doc(messageId);
    return messageRef.delete();
}

async function lookupReceiverId(friendEmail)
{
    const userRecord = await admin.auth().getUserByEmail(friendEmail);
    console.log('Found user. Id: ', userRecord.uid);
    return userRecord.uid;
}

async function getReceiverUserProfile(receiverId)
{
    let receiverRef = db.collection('users').doc(receiverId);
    const receiverUserDoc = await receiverRef.get();
    const receiverUserProfile = receiverUserDoc.data();
    // check if doc does not exist here?
    return receiverUserProfile;
}

async function sendFriendRequest(data, receiverUserProfile)
{
    const receiverDeviceToken = receiverUserProfile.deviceToken;
    const messageType = "friendRequest";

    let message = {
        data: {
            messageType: messageType,
            actionType: data.actionType,
            friendEmail: data.friendEmail,
            senderId: data.senderId,
            senderDisplayName: data.senderDisplayName,
            senderDeviceToken: data.senderDeviceToken,
            receiverDisplayName: receiverUserProfile.displayName,
            toDoGroupId: data.toDoGroupId,
            toDoGroupTitle: data.toDoGroupTitle
        },
        token: receiverDeviceToken
    };

    console.log('Sending friend request. message: %j', message);

    const response = await admin.messaging().send(message);
    console.log('Successfully sent friend request. System message ID:', response);
    return response;
}

async function filterMessageType(data, receiverUserProfile)
{
	const messageType = data.messageType;

	switch (messageType) {
		case "friendActionResponse":
            if (data.actionType == "acceptFriend")
            {
                try {
                    connectFriends(data);
                }
                catch (error) {
                    console.log('Error syncing new friends in cloud: ', error);
                    // TODO: notify users of error
                    return;
                }
            }
            else if (data.actionType == "denyFriend")
            {
                // ...
            }

            // send notification to original friend request sender
            return sendFriendNotify(data, receiverUserProfile);

        case "shareToDoRequest":
        	try {
        		sendShareToDoRequest(data, receiverUserProfile);
        	}
        	catch (error) {
        		console.log('Error sending share to do request: ', error);
        		// TODO: notify user of error
        	}

        	return sendFriendNotify(data, receiverUserProfile);

        case "shareToDoActionResponse":
        	if (data.actionType == "acceptToDoList")
        	{
        		try {
        			shareToDoList(data);
        		}
        		catch (error) {
        			console.log('Error adding new user to to do list in cloud: ', error);
        			// TODO: notify users of error
        			return;
        		}
        	}

        	return sendFriendNotify(data, receiverUserProfile);

		case "placeholder":
            return;
	}

	return true;
}

/* Updates friends data in user profiles in FireStore */
async function connectFriends(data)
{
    const senderId = data.senderId;
    const receiverId = data.receiverId;

    let senderUserDocRef = db.collection('users').doc(senderId);
    let receiverUserDocRef = db.collection('users').doc(receiverId);

    let batch = db.batch();

    batch.update(senderUserDocRef, {
        ['friendListMap.' + receiverId]: {
            friendDisplayName: data.receiverDisplayName
        }
    }, {merge: true});

    batch.update(receiverUserDocRef, {
        ['friendListMap.' + senderId]: {
            friendDisplayName: data.senderDisplayName
        }
    }, {merge: true});

    const commitBatch = await batch.commit();

    return true;
}

/* Updates to do list and user profiles in FireStore */
async function shareToDoList(data)
{
	const senderId = data.senderId;
    const receiverId = data.receiverId;
    const toDoGroupId = data.toDoGroupId;

    let senderUserDocRef = db.collection('users').doc(senderId);
    let receiverUserDocRef = db.collection('users').doc(receiverId);
    let toDoGroupDocRef = db.collection('todogroups').doc(toDoGroupId);

    let batch = db.batch();

    batch.update(senderUserDocRef, {
    	subscriptions: admin.firestore.FieldValue.arrayUnion(toDoGroupId)
    });

    batch.update(toDoGroupDocRef, {
    	subscribers: admin.firestore.FieldValue.arrayUnion(senderId)
    })

    const commitBatch = await batch.commit();

    return true;
}

/* Sends a notification to user who originally sent the request.
   actionType contains whether it was accepted or denied. */
async function sendFriendNotify(data, receiverUserProfile)
{
    const receiverDeviceToken = receiverUserProfile.deviceToken;
    const messageType = "friendNotify";

    let message = {
        data: {
            messageType: messageType,
            actionType: data.actionType,
            friendEmail: data.friendEmail,
            senderId: data.senderId,
            senderDisplayName: data.senderDisplayName,
            senderDeviceToken: data.senderDeviceToken,
            receiverDisplayName: receiverUserProfile.displayName,
            toDoGroupId: data.toDoGroupId,
            toDoGroupTitle: data.toDoGroupTitle
        },
        token: receiverDeviceToken
    };

    console.log('Sending friend notify message: %j', message);

    const response = await admin.messaging().send(message);
    console.log('Successfully sent friend notify message. System message ID:', response);
    return response;
}

async function sendShareToDoRequest(data, receiverUserProfile)
{
    const receiverDeviceToken = receiverUserProfile.deviceToken;

    let message = {
        data: {
            messageType: data.messageType,
            actionType: data.actionType,
            friendEmail: data.friendEmail,
            senderId: data.senderId,
            senderDisplayName: data.senderDisplayName,
            senderDeviceToken: data.senderDeviceToken,
            receiverDisplayName: receiverUserProfile.displayName,
            toDoGroupId: data.toDoGroupId,
            toDoGroupTitle: data.toDoGroupTitle
        },
        token: receiverDeviceToken
    };

    console.log('Sending share to do list request. message: %j', message);

    const response = await admin.messaging().send(message);
    console.log('Successfully sent share to do list request. System message ID:', response);
    return response;
}