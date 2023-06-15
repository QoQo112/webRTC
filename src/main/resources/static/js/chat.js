let websocket = null;
window.onload = function() {
        conectWebSocket();
}
function conectWebSocket(){
//判断当前浏览器是否支持WebSocket
let userId = document.getElementById("userId").value;
//userId從後端來，加載頁面就把userId、websocket連上去。
if ('WebSocket'in window) {
websocket = new WebSocket("ws://localhost:8080/websocket/");
} else {
alert('Not support websocket')
}
//连接发生错误的回调方法
websocket.onerror = function() {
setMessageInnerHTML("error");
};
//连接成功建立的回调方法
websocket.onopen = function(event) {
setMessageInnerHTML("Loc MSG: 成功建立連結");
}
//接收到消息的回调方法
websocket.onmessage = function(event) {
setMessageInnerHTML(event.data);
}
//连接关闭的回调方法
websocket.onclose = function() {
setMessageInnerHTML("Loc MSG:關閉連結");
}
//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
window.onbeforeunload = function() {
websocket.close();
}
}
//将消息显示在网页上
function setMessageInnerHTML(innerHTML) {
document.getElementById('message').innerHTML += innerHTML + '<br/>';
}
//关闭连接
function closeWebSocket() {
websocket.close();
}
//发送消息
function send() {
let message = document.getElementById("text").value;
let channel = document.getElementById("channel").value;
var socketMessage = {message:message, receiver:channel};
if(channel === "") {
socketMessage.type = 0;
} else {
socketMessage.type = 1;
}
console.log(typeof socketMessage);
websocket.send(JSON.stringify(socketMessage));

}