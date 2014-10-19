Sample-Socket-Server

A Sample Socket Server with the support of handling multiple HTTP(https://www.ietf.org/rfc/rfc2616.txt) clientRequests / connections with out using any libraries by doing all the operations like starting the server socket, listening on it, spawning a thread for every client who connects..etc.

Doesn’t use any external libraries and is written from scratch.

Current 3 major APIs 

1: Exposes a GET API as "api/request?connId=12&timeOut=100" : This API will keep the request running for provided time on the server side. After the successful completion of the provided time it returns a successful response

2: Exposes a GET API as "api/serverStatus" - This API returns all the running requests on the server with their time left for completion.

3: Exposes a PUT API as "api/kill" with payload as {"connId":12} this will kill/finish the running request with provided connId, so that the finished request returns got_killed_response and the current request will return a successful response with the connectionId of the killed request. If no running request found with the provided connId on the server then the current request should return connId_not_found response

//Handles all the end cases and exception scenarios like malformed requestHeaders, unsupported protocols, unsupported Methods + URL combos, malformed URLs, wrong payloads, missing query params, connId already being connected, connId not present to kill, IOExceptions..etc with proper response message being sent to Client

//Tested from Java, Chrome REST Client, CURL library

//To start the server, need to launch org.gandhi.sample.socket.server.run.ServerLauncher;
