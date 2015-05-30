var previousCommandsRegister = [];
$(document).ready(function () {
    function initializeCommands() {
        new _command(/^(\/nick)/, "/nick", function (args) {
            if (args.length > 1 && nickname != args) {
                emitMessageAs([nickname, " zmienił nick na ", args].join(""), "System", "#000000");

                nickname = args;
                ls.setItem("nick", args);

                unsubChat();
                poll();
            }
            else {
                appendMessage("Wpisałeś nowy nick?");
            }
        }, " Aby zmienić nick");
        new _command(/^(\/color)/, "/color", function (args) {
            if (args.length > 1) {
                if (/(^#[0-9A-F]{6}$)|(^#[0-9A-F]{3}$)/i.test(args)) {
                    currentColor = args;
                    ls.setItem("color", args);
                    appendMessage(["Nowy kolor to ", '<span style="color:', args, '">', args, "</span>"].join(""));
                    return false;
                }
                else {
                    appendMessage("Podałeś zły kolor");
                    return true;
                }
            }
            else {
                appendMessage("Nie podałeś koloru");
                return true;
            }
        }, " Aby zmienić kolor");
        new _command(/^(\/empty)/, "/empty", function (args) {
            $("#messages").empty();
        }, " Aby wyczyscic widok chatu");
        new _command(/^(\/changeChannel)/, "/changeChannel", function (args) {
            if (args.length > 0) {
                unsubChat();
                currentChannel = args;
                $("#messages").empty();
                poll();
                appendMessage("Nowy jesteś teraz na kanale: " + args)
            }
        }, " Aby zmienic kanal, np. /changeChannel nazwaNowegoKanalu");
        new _command(/^(\/exit)/, "/exit", function (args) {
            unsubChat();
        }, " Aby opuscic chat.");
        new _command(/^(\/connect)/, "/connect", function (args) {
            unsubChat();
        }, " Aby ponownie dolaczyc do chatu");
        new _command(/^(\/help)/, "/help", function (args) {
            var help = [];
            for (var i = 0; i < allCommands.length; i++) {
                help.push(allCommands[i].string.concat(" - ").concat(allCommands[i].description).concat("<br/>"))
            }
            appendMessage(help.join(""))
        }, " Aby wyświetlić pomoc");
        new _command(/^(\/whoisactive)/, "/whoisactive", function (args) {
            getListOfActiveUsers();
        }, " Aby zobaczyc uzytkownikow online");
        new _command(/^(\/pw)/, "/pw", function (args) {
            var sliced = args.split("'");

            var name = sliced[1];
            var message = sliced[2];

            if(name.length > 0 && message.length > 0){
                var toBeSent = {
                    name: name,
                    channel: currentChannel,
                    color: "#000000",
                    chatMessage: nickname+ " whispers:"+message,
                    currentTime: new Date().getTime()
                };
                $.ajax({
                    url: "pw",
                    method: "POST",
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    traditional: true,
                    success: function (data) {
                        console.log(data);
                    },
                    data: JSON.stringify(toBeSent)
                });
                appendMessage("Whispered to "+name+" : "+message)
            } else {
                console.log(name.length + " " + message.length)
                console.log(sliced)
            }
        }, " Aby wysłać prywatną wiadomosc do 'Downolnego użytkownika' ")
    }

    function initializeAutoComplete() {
        for (var i = 0; i < allCommands.length; i++) {
            autocomplete.push(allCommands[i].string);
        }
        $("#m").autocomplete({
            source: autocomplete
        })
    }

    $("#forms").on("keydown", function (event) {
        var targetRef = $(event.target);
        var previous = parseInt(targetRef.attr("data-previous-command"));
        switch (event.which) {
            case 38:
                if (previous < previousCommandsRegister.length) {
                    console.log("Trying to set ");
                    targetRef.val(previousCommandsRegister[previous]);
                    targetRef.attr("data-previous-command", previous + 1)
                }
                break;
            case 40:
                if (previous > 0) {
                    console.log("Trying to set ");
                    targetRef.val(previousCommandsRegister[previous - 1]);
                    targetRef.attr("data-previous-command", previous - 1)
                } else {
                    targetRef.val("")
                }
                break;
        }
    });

    initializeCommands();
    initializeAutoComplete();
});

var allCommands = [];
var autocomplete = [];

function applyToCommands(string) {
    if (string.length > 0) {
        var magicFlag = true;
        $.each(allCommands, function (index, element) {
            if (element.apply(string)) {
                previousCommandsRegister.push(string);
                magicFlag = false;
                return false;
            }
        });
        if (magicFlag) {
            emitMessage(string);
        }
    }
}

function _command(regexp, string, action, description) {
    this.regexp = regexp;
    this.action = action;
    this.string = string;
    this.description = description;

    allCommands.push(this);
}

_command.prototype.apply = function (string) {
    if (this.regexp.test(string)) {
        this.action(string.substr(this.string.length + 1));
        console.log(string.substr(this.string.length + 1));
        return true;
    } else {
        return false;
    }

};
