var WebClient = require('@slack/client').WebClient;

var token = process.env.SLACK_API_TOKEN || ''; //see section above on sensitive data

var web = new WebClient(token);

web.channels.list(function(err, info) {
   if (err) {
       console.log('Error:', err);
   } else {
       for(var i in info.channels) {
           console.log(info.channels[i]);
       }
   }
});
