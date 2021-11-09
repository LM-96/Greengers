const util = require('util');
const express = require('express');
var querystring = require('querystring');
const app = express();

// Includiamo la libreria "body-parser" per gestire le richieste in JSON.
const bodyparser = require('body-parser');
app.use(bodyparser.json());

// Includiamo il modulo "request" per effettuare richieste HTTP
const https = require('https');

var usersArray=[{
  user_chatid: "449963751",
  chat_status: 0,
  action_to_do: 0
}];

function printAllUsers(){
  console.log("Numero utenti: "+usersArray.length+"\n");
  for(var i=0; i < usersArray.length;i++){
    console.log("user_chatid: "+usersArray[i].user_chatid);
    console.log("chat_status: "+usersArray[i].chat_status);
    console.log("action_to_do: "+usersArray[i].action_to_do);
    console.log("\n");
  }
}

function addNewUser(chatid){
  var exist=0;
  for(var i=0; i < usersArray.length; i++){
    if(usersArray[i].user_chatid == chatid){
      exist = 1;
    }
  }
  if(exist==1){
     console.log("Utente già registrato.");
  }else{
    usersArray.push({
      user_chatid: chatid,
      chat_status: 0,
      action_to_do: 0
    });
    console.log("Nuovo utente aggiunto.");
  }
}

function alterChatStatus(chatid, chatStatus){
  for(var i=0; i < usersArray.length; i++){
    if(usersArray[i].user_chatid == chatid){
      usersArray[i].chat_status = chatStatus;
    }
  }
}

function alterActionToDo(chatid, actionToDo){
  for(var i=0; i < usersArray.length; i++){
    if(usersArray[i].user_chatid == chatid){
      usersArray[i].action_to_do = actionToDo;
    }
  }
}

function getUser(chatid){
  for(var i=0; i < usersArray.length; i++){
    if(usersArray[i].user_chatid == chatid){
      return usersArray[i];
    }
  }
}

// Webhook per Telegram
app.post('/telegram', (req, res) => {
  
	console.log("Richiesta: " + JSON.stringify(req.body));
	const chatid = req.body.message.chat.id;
  const text = req.body.message.text;
  const clientid = req.body.message.from.id;
	
	console.log("Utente in chat " + chatid + " ha scritto '" + text + "'");
  
  addNewUser(chatid);
  alterChatStatus(chatid, 1);
  
  if(text == "/start"){
    sendText(chatid, "Benvenuto nel bot. Digita il comando /help per visualizzare i possibili comandi che il bot mette a disposizione.");
    alterChatStatus(chatid, 0);
  }
  if(text == "/searchsongbyparameter"){
    sendText(chatid, "Digita i termini con cui eseguire la ricerca");
    alterChatStatus(chatid, 0);
    alterActionToDo(chatid, 1);
    //checkTokenValidity(process.env.SPOTIFY_ACCESS_TOKEN);
  }
  if(text == "/getartistpagebyname"){
    sendText(chatid, "Digita il nome dell'autore");
    alterChatStatus(chatid, 0);
    alterActionToDo(chatid, 2);
    //checkTokenValidity(process.env.SPOTIFY_ACCESS_TOKEN);
  }
  if(text == "/searchyoutubevideos"){
    sendText(chatid, "Digita i termini della ricerca");
    alterChatStatus(chatid, 0);
    alterActionToDo(chatid, 3);
    //checkTokenValidity(process.env.SPOTIFY_ACCESS_TOKEN);
  }
  if(text == "/searchsongonspotify"){
    if(clientid == process.env.ADMIN_ID){
      sendText(chatid, "Digita i termini della ricerca");
      alterChatStatus(chatid, 0);
      alterActionToDo(chatid, 4);
      checkTokenValidity(process.env.SPOTIFY_ACCESS_TOKEN);
    }else{
      sendText(chatid, "Funzione riservata all'utente admin.");
      alterChatStatus(chatid, 0);
      alterActionToDo(chatid, 0);
    }
  }
  if(text == "/help"){
    //checkTokenValidity(process.env.SPOTIFY_ACCESS_TOKEN);
    alterChatStatus(chatid, 1);
    alterActionToDo(chatid, 5);
  }
  
  var user = getUser(chatid);
  console.log(user);
  
  if(user.chat_status == 1){
    if((user.action_to_do == 1) || 
       (user.action_to_do == 2) ||
       (user.action_to_do == 3) ||
       (user.action_to_do == 4) ||
       (user.action_to_do == 5)){
      if(user.action_to_do == 1){
        console.log("Eseguirò la richiesta getMusicByParameter con termini di ricerca '"+text+"'");
        sendRequestToPlatform(chatid, text, user.action_to_do, process.env.SPOTIFY_ACCESS_TOKEN);
        alterActionToDo(chatid, 0);
      }
      if(user.action_to_do == 2){
        console.log("Eseguirò la richiesta getArtistPageByName con termini di ricerca '"+text+"'");
        sendRequestToPlatform(chatid, text, user.action_to_do, process.env.SPOTIFY_ACCESS_TOKEN);
        alterActionToDo(chatid, 0);
      }
      if(user.action_to_do == 3){
        console.log("Eseguirò la richiesta searchYoutubeVideos con termini di ricerca '"+text+"'");
        sendRequestToPlatform(chatid, text, user.action_to_do, process.env.SPOTIFY_ACCESS_TOKEN);
        alterActionToDo(chatid, 0);
      }
      if(user.action_to_do == 4){
        console.log("Eseguirò la richiesta searchSongOnSpotify con termini di ricerca '"+text+"'");
        sendRequestToPlatform(chatid, text, user.action_to_do, process.env.SPOTIFY_ACCESS_TOKEN);
        alterActionToDo(chatid, 0);
      }
      if(user.action_to_do == 5){
        console.log("Eseguirò la richiesta del comando di help");
        sendRequestToPlatform(chatid, text, user.action_to_do, process.env.SPOTIFY_ACCESS_TOKEN);
        alterActionToDo(chatid, 0);
      }
    }else{
      sendText(chatid, "Comando non disponibile.");
    }
  }
	res.end();
  
});

const listener = app.listen(process.env.PORT, function() {
  console.log('Your app is listening on port ' + listener.address().port);
});

function sendText(chatId, text){
  const requestBody = { 
	      chat_id: chatId,
	      text: text
  }
  
  const clientreq = https.request({
    method: 'POST',
    host: 'api.telegram.org',
    path: '/bot' + process.env.BOTTOKEN + '/sendMessage',
    headers: {
	    'Content-Type':'application/json',
    },	  
  }, function(resp) {
    // Questa funzione viene richiamata a richiesta eseguita
    if(resp.statusCode != 200) {
      console.log(resp.statusCode);
      return;
    }  
  });
  clientreq.write(JSON.stringify(requestBody));	
  clientreq.end(); // questa chiamata esegue la richiesta
}

function getNewAccessToken(){
  var bodyRequest = querystring.stringify({
    grant_type: 'refresh_token',
    refresh_token: process.env.SPOTIFYKEYREFRESH
  });
  
  const clientreq = https.request({
    method: 'POST',
    host: 'accounts.spotify.com',
    path: '/api/token',
    headers: {
      'Content-Type':'application/x-www-form-urlencoded',
	    'Authorization':'Basic '+process.env.SPOTIFYBASE64ID
    },	  
  }, function(resp) {
    // Questa funzione viene richiamata a richiesta eseguita
    if(resp.statusCode != 200) {
      console.log("Richiesta HTTP Spotify NewToken fallita");
      console.log(resp.statusCode);
      return;
    }
    console.log("Richiesta HTTP Spotify NewToken riuscita");
    
    var body = '';
    resp.on('data', function(d) {
        body += d;
    });
    resp.on('end', function() {
      
      const j = JSON.parse(body);
      var token = j.access_token;
      process.env.SPOTIFY_ACCESS_TOKEN = token;
      console.log("Nuovo token ottenuto.");
    });
  });
  //console.log(clientreq);
  clientreq.write(bodyRequest);
  clientreq.end(); // questa chiamata esegue la richiesta
}

function checkTokenValidity(token){
  var searchString = "prova";
  const clientreq = https.request({
    method: 'GET',
    host: 'api.spotify.com',
    path: '/v1/search?q='+searchString+'&type=track&limit=5&access_token='+token,
    headers: {
	    'Content-Type':'application/json',
    },	  
  }, function(resp) {
    // Questa funzione viene richiamata a richiesta eseguita
    if(resp.statusCode != 200) {
      getNewAccessToken();
    }else{
      console.log("Token valido.");
      console.log(process.env.SPOTIFY_ACCESS_TOKEN);
    }
  });
  clientreq.end(); // questa chiamata esegue la richiesta
}

function sendRequestToPlatform(chatId, text, actionToDo, token){
  
  var host = "piattaformacontenutimusicali.herokuapp.com";
  
  if(actionToDo == 1){
    var searchString = text;
    searchString = searchString.replace(/\s/g,"+");
    var path = "/searchiTunesSong/"+searchString;
  }
  if(actionToDo == 2){
    var searchString = text;
    searchString = searchString.replace(/\s/g,"+");
    var path = "/searchiTunesArtist/"+searchString;
  }
  if(actionToDo == 3){
    var searchString = text;
    searchString = searchString.replace(/\s/g,"+");
    var path = "/searchYoutubeVideos/"+searchString;
  }
  if(actionToDo == 4){
    var searchString = text;
    searchString = searchString.replace(/\s/g,"+");
    var path = "/searchSongOnSpotify/"+searchString+"/"+token;
  }
  if(actionToDo == 5){
    var path = "/help";
  }
  
  const clientreq = https.request({
    method: 'GET',
    host: host,
    path: path,
    headers: {
	    'Content-Type':'application/json',
    },	  
  }, function(resp) {
    // Questa funzione viene richiamata a richiesta eseguita
    if(resp.statusCode != 200) {
      return;
    }  
    var body = '';
    resp.on('data', function(d) {
        body += d;
    });
    resp.on('end', function() {
      
      const j = JSON.parse(body);
      console.log(j);
      
      var string='';
      if(j.tipoRisultato == "text"){
        sendText(chatId, j.text);
      }else{
        if(j.risultatiTotali == 0){
          sendText(chatId, "Nessun risultato disponibile.");
        }else{
          string+="Risultati";
          for(var i=0; i<j.risultatiTotali; i++){
            string+="\n\n● Nome: "+j.items[i].nome;
            if(j.items[i].album != 0)
              string+="\n   Album: "+j.items[i].album;
            if(j.items[i].autore != 0)
              string+="\n   Autore: "+j.items[i].autore;
            if(j.items[i].prezzo != 0)
              string+="\n   Prezzo: "+j.items[i].prezzo;
            string+="\n   Link: "+j.items[i].link;
          }
        }
        sendText(chatId, string);
      }
    });
    
  });	
  clientreq.end(); // questa chiamata esegue la richiesta
}