$(document).ready(function () {
    function initializeCommands(){
        new _command(/^(\/nick)/, "/nick" , function(args){
            if (args.length > 1 && nickname != args) {
                emitMessageAs([nickname," zmienił nick na ",args].join(""), "System", "#000000");

                nickname = args;
                ls.setItem("nick", args);

                unsubChat();
                poll();
            }
            else {
                appendMessage("Wpisałeś nowy nick?");
            }
        });
        new _command(/^(\/color)/,"/color",function(args){
            if (args.length > 1) {
                if (/(^#[0-9A-F]{6}$)|(^#[0-9A-F]{3}$)/i.test(args)) {
                    currentColor = args;
                    ls.setItem("color", args);
                    appendMessage(["Nowy kolor to ", '<span style="color:',args,'">',args,"</span>"].join(""));
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
        });
        new _command(/^(\/empty)/,"/color",function(args){
            $("#messages").empty();
        });
        new _command(/^(\/changeChannel)/,"/changeChannel",function(args){
            unsubChat();
            currentChannel = args;
            poll();
            appendMessage("Nowy jesteś teraz na kanale: " + args)
        });
        new _command(/^(\/exit)/,"/exit",function(args){
            unsubChat();
        });
    }

   initializeCommands();
});

var allCommands = [];



function applyToCommands(string){
    var magicFlag = true;
    $.each(allCommands, function (index,element) {
        if(element.apply(string)){
            magicFlag = false;
            return false;
        }
    });
    if(magicFlag)
        emitMessage(string);
}

function _command(regexp,string,action){
    this.regexp = regexp;
    this.action = action;
    this.string = string;

    allCommands.push(this);
}

_command.prototype.apply = function (string) {
  if(this.regexp.test(string)){
      this.action(string.substr(this.string.length+1));
      console.log(string.substr(this.string.length+1));
      return true;
  } else {
      return false;
  }

};
