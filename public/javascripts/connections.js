$(document).ready(function () {
    $("#chatContainer").perfectScrollbar();

    window.onbeforeunload = function (e) {
        var e = e || window.event;

        //IE & Firefox
        if (e) {
            e.returnValue = 'Are you sure?';
        }
        // For Safari
        return 'Czy na pewno chcesz opuscic chat' + unsubChat();
    };

    $('#forms').submit(function () {
        var value = $('#m').val();
        applyToCommands(value);
        $('#m').val('');
        return false;
    });
});
function appendMessage(msg) {
    $('#messages').append($('<li>').html(msg));
}
function parseAndAppendMessage(incomingObject) {
    $('#messages').append($('<li>')
            .append($("<span>").html(incomingObject.name + ":").css("color", incomingObject.color))
            .append($("<p>").html(incomingObject.chatMessage).emoticonize())
    );
    lastMessage = incomingObject.id > lastMessage ? incomingObject.id : lastMessage;
}
function unsubChat() {
    connection.abort();
    var toBeSent = {
        name: nickname,
        color: currentColor,
        chatMessage: [nickname, " opuścił chat"].join(""),
        currentTime: new Date().getTime(),
        channel: currentChannel
    };
    $.ajax({
        method: "POST",
        url: "unsub",
        data: JSON.stringify(toBeSent),
        dataType: "json",
        contentType: "application/json; charset=utf-8"
    });
    return "Poszło"
}
function poll() {
    var toBeSent = {
        name: nickname,
        channel: currentChannel,
        currentTime: lastMessage
    };
    connection = $.ajax({
        method: "POST",
        url: "poll",
        success: function (data) {
            console.log("SUCCESS " + JSON.stringify(data));
            $.each(data, function (index, element) {
                parseAndAppendMessage(element)
            });
            poll();
        },
        data: JSON.stringify(toBeSent),
        dataType: "json",
        contentType: "application/json; charset=utf-8"
    })
}

function emitMessageAs(data, nick, color) {
    var toBeSent = {
        name: nick,
        channel: currentChannel,
        color: color,
        chatMessage: data,
        currentTime: new Date().getTime()
    };
    $.ajax({
        url: "sendMessage",
        method: "POST",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        traditional: true,
        success: function (data) {
            console.log(data);
        },
        data: JSON.stringify(toBeSent)
    });
    console.log(toBeSent)
}

function emitMessage(data) {
    emitMessageAs(data, nickname, currentColor)
}

$(document).ready(function () {
    poll();
});