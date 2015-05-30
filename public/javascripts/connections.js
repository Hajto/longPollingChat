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
function scrollAndRefresh(){
    $('html, body').animate({
        scrollTop: $(document).height()
    }, 1000);
    refreshScrollbar($(window).width(),$(window).height()-50)
}
function appendMessage(msg) {
    $('#messages').append($('<li>').html(msg));
}
function parseAndAppendMessage(incomingObject) {
    $('#messages').append($('<li>')
            .append($("<span>").html(incomingObject.name + ":").css("color", incomingObject.color))
            .append($("<p>").html(incomingObject.chatMessage).emoticonize())
    );
    scrollAndRefresh();
    console.log(incomingObject.id);
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
        },
        complete: function(error){
            poll()
        },
        data: JSON.stringify(toBeSent),
        dataType: "json",
        contentType: "application/json; charset=utf-8"
    })
}

function getListOfActiveUsers(){
    $.ajax({
        method: "GET",
        url: "listusers",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            console.log(response);
            cos = response
            var accumulator = "Online: ";
            for(var i=0; i < response.length; i++){
                //TODO: Lagpoint poprawic, ale teraz nie chce mi sie z tym bawic, z niewiadomych powodow concat nie dziala
                accumulator += response[i] + " "
            }
            appendMessage(accumulator)
        }
    });
}

//TODO: Zrobić, tak aby ta funkcja nie była konieczna, bo jest bardzo niebezpieczna, ale w tym konkretnym momencie mi się nie chce.
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
}

function emitMessage(data) {
    emitMessageAs(data, nickname, currentColor)
}

$(document).ready(function () {
    poll();
});