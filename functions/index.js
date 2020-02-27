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
                return sendFriendRequest(messageId, data, receiverUserProfile);
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
                filterMessageType(messageId, data, messageType, receiverUserProfile);
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

async function sendFriendRequest(messageId, data, receiverUserProfile)
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
            toDoGroupId: data.toDoGroupId
        },
        token: receiverDeviceToken
    };

    console.log('Sending friend request. message: %j', message);

    const response = await admin.messaging().send(message);
    console.log('Successfully sent friend request. System message ID:', response);
    return response;
}

async function connectFriends(messageId, data, receiverUserProfile)
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

/* Sends a notification to user who originally sent friend request.
 * actionType contains whether it was accepted or denied.
*/
async function sendFriendNotify(messageId, data, receiverUserProfile)
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
            toDoGroupId: data.toDoGroupId
        },
        token: receiverDeviceToken
    };

    console.log('Sending friend request. message: %j', message);

    const response = await admin.messaging().send(message);
    console.log('Successfully sent friend request. System message ID:', response);
    return response;
}


async function filterMessageType(messageId, data, messageType, receiverUserProfile)
{
	switch (messageType) {
		case "friendActionResponse":
            if (data.actionType == "acceptFriend")
            {
                try {
                    connectFriends(messageId, data, receiverUserProfile);
                }
                catch (error) {
                    console.log('Error syncing new friends in cloud: ', error);
                    // notify users of error
                    return;
                }
            }
            else if (data.actionType == "denyFriend")
            {
                // ...
            }

            // send notification to original friend request sender

            return sendFriendNotify(messageId, data, receiverUserProfile);

		case "placeholder":
            return;
	}

	return true;
}