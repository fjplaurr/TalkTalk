/*
 * Functions SDK : is required to work with firebase functions.
 * Admin SDK : is required to send Notification using functions.
 */

'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
// admin.initializeApp(functions.config().firebase);


/*
 * 'OnWrite' works as 'addValueEventListener' for android. It will fire the function
 * everytime there is some item added, removed or changed from the provided 'database.ref'
 * 'sendNotification' is the name of the function, which can be changed according to
 * your requirement
 */

exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}').onWrite((change,context) => {


  /*
   * You can store values as variables from the 'database.ref'
   * Just like here, I've done for 'user_id' and 'notification'
   */

  //const user_id = event.params.user_id;
  const user_id=context.params.user_id;

  // const notification_id = event.params.notification_id;
  const notification_id = context.params.notification_id;

  console.log('We have a notification to send to : ', user_id);

  /*
   * onWrite es un una interfaz que se va a ejecutar tanto cuando se añade una notificación como cuando se elimina. 
   * Pero yo no quiero que el usuario reciba una notificación cuando se elimina una notificación de la BD. Por tanto, 
   * agrego el siguiente if que se ejecutará si el event está vacío (es decir, ha sido eliminada la notificación).
   */

if(!change.after.val()){
  	return console.log('A Notification has been deleted from the database : ',notification_id);
  }
  /*if(!event.data.val()){
  	return console.log('A Notification has been deleted from the database : ',notification_id);
  }*/

  const fromUser=admin.database().ref(`/notifications/${user_id}/${notification_id}`).once('value');
  
  return fromUser.then(fromUserResult=>{
  	const from_user_id=fromUserResult.val().from;
  	console.log('You have a new notification from: ',from_user_id);

  	const userQuery=admin.database().ref(`Users/${from_user_id}/name`).once('value');
  	return userQuery.then(userResult =>{

  		const userName=userResult.val();

  		const deviceToken=admin.database().ref(`/Users/${user_id}/device_token`).once('value');

  		return deviceToken.then(result =>{
	  		const token_id=result.val();
	  		const payload = {
	  			notification: {
		  			title : "New Friend Request",
		  			body: `${userName} has sent you a request`,
		  			icon: "default",
		  			click_action:"TalkTalk_notify"
		  		},
		  		data:{
		  			from_user_id: from_user_id  /*Aquí creo una variable llamada from_user_id y le doy el valor de la variable q 
		  											*q se llama igual para poder enviarla a la clase FirebaseMessagingService.
		  											*/
		  		}
		  	};

	      /*
	       * Then using admin.messaging() we are sending the payload notification to the token_id of
	       * the device we retreived.
	       */

	       return admin.messaging().sendToDevice(token_id, payload).then(response => {
	       	console.log('This was the notification Feature');
	       });
	       
	   });

  	});

  }); 

});