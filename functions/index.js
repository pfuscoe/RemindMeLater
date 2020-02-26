// Google Cloud Functions for Remind Me Later Firestore Event Triggers

/** WORK IN PROGRESS **/

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();


// Listen for writes to messages and call appropriate function
exports.messageListener = functions.firestore
    .document('messages/{messageId}').onWrite(async (change, context) => {
        const messageId = context.params.messageId;
        const data = change.after.data();
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
                const receiverDeviceToken = data.receiverDeviceToken;
                filterMessageType(messageId, data, messageType, receiverUserProfile,
                    receiverDeviceToken);
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
    // check if doc does not exist here?
    return receiverUserProfile = receiverUserDoc.data();
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


function filterMessageType(messageId, data, messageType, receiverUserProfile,
    receiverDeviceToken) {

	switch (messageType) {
		case "friendActionResponse":
            if (data.actionType == "acceptFriend")
            {
                try {
                    const connectFriends = await connectFriends(messageId, data,
                        messageType, receiverUserProfile, receiverDeviceToken);
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

            const sendFriendNotify = await sendFriendNotifyNotification(messageId, 
                data, messageType, receiverUserProfile, receiverDeviceToken);

            return;

		case "placeholder":
            return;
	}
}

async function connectFriends(messageId, data, messageType, receiverUserProfile,
    receiverDeviceToken)
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

    return batch.commit();
}

async function sendFriendNotifyNotification(messageId, data, messageType, 
    receiverUserProfile, receiverDeviceToken)
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

/*
let getDoc = cityRef.get()
  .then(doc => {
    if (!doc.exists) {
      console.log('No such document!');
    } else {
      console.log('Document data:', doc.data());
    }
  })
  .catch(err => {
    console.log('Error getting document', err);
  });
  */

// Receive request to add friend and send confirmation request to other user


// Receive confirmation to add friend and send message to first user.
// Update both users' friends list




// Receive request to share To Do List with friend and send confirmation to other user



// Receive confirmation to add shared To Do List and send message to first user.
// Update shared field in To Do List, add new user to subscribers
// Update new user's subscriptions



// Receive request to send reminder copy to other user and send the reminder


// Receive confirmation to add reminder copy and send message to first user

/*

// Listen for any change on document `marie` in collection `users`
exports.myFunctionName = functions.firestore
    .document('users/marie').onWrite((change, context) => {
      // ... Your code here
    });

// Listen for changes in all documents in the 'users' collection
exports.useWildcard = functions.firestore
    .document('users/{userId}')
    .onWrite((change, context) => {
      // If we set `/users/marie` to {name: "Marie"} then
      // context.params.userId == "marie"
      // ... and ...
      // change.after.data() == {name: "Marie"}
    });

// Listen for changes in all documents in the 'users' collection and all subcollections
exports.useMultipleWildcards = functions.firestore
    .document('users/{userId}/{messageCollectionId}/{messageId}')
    .onWrite((change, context) => {
      // If we set `/users/marie/incoming_messages/134` to {body: "Hello"} then
      // context.params.userId == "marie";
      // context.params.messageCollectionId == "incoming_messages";
      // context.params.messageId == "134";
      // ... and ...
      // change.after.data() == {body: "Hello"}
    });

exports.createUser = functions.firestore
    .document('users/{userId}')
    .onCreate((snap, context) => {
      // Get an object representing the document
      // e.g. {'name': 'Marie', 'age': 66}
      const newValue = snap.data();

      // access a particular field as you would any JS property
      const name = newValue.name;

      // perform desired operations ...
    });

exports.updateUser = functions.firestore
    .document('users/{userId}')
    .onUpdate((change, context) => {
      // Get an object representing the document
      // e.g. {'name': 'Marie', 'age': 66}
      const newValue = change.after.data();

      // ...or the previous value before this update
      const previousValue = change.before.data();

      // access a particular field as you would any JS property
      const name = newValue.name;

      // perform desired operations ...
    });

exports.deleteUser = functions.firestore
    .document('users/{userID}')
    .onDelete((snap, context) => {
      // Get an object representing the document prior to deletion
      // e.g. {'name': 'Marie', 'age': 66}
      const deletedValue = snap.data();

      // perform desired operations ...
    });

exports.modifyUser = functions.firestore
    .document('users/{userID}')
    .onWrite((change, context) => {
      // Get an object with the current document value.
      // If the document does not exist, it has been deleted.
      const document = change.after.exists ? change.after.data() : null;

      // Get an object with the previous document value (for update or delete)
      const oldDocument = change.before.data();

      // perform desired operations ...
    });

exports.updateUser = functions.firestore
    .document('users/{userId}')
    .onUpdate((change, context) => {
      // Get an object representing the current document
      const newValue = change.after.data();

      // ...or the previous value before this update
      const previousValue = change.before.data();
    });

// Fetch data using standard accessors
const age = snap.data().age;
const name = snap.data()['name'];

// Fetch data using built in accessor
const experience = snap.get('experience');

// Listen for updates to any `user` document.
exports.countNameChanges = functions.firestore
    .document('users/{userId}')
    .onUpdate((change, context) => {
      // Retrieve the current and previous value
      const data = change.after.data();
      const previousData = change.before.data();

      // We'll only update if the name has changed.
      // This is crucial to prevent infinite loops.
      if (data.name == previousData.name) return null;

      // Retrieve the current count of name changes
      let count = data.name_change_count;
      if (!count) {
        count = 0;
      }

      // Then return a promise of a set operation to update the count
      return change.after.ref.set({
        name_change_count: count + 1
      }, {merge: true});
    });

*/

