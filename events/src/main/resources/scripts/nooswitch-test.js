var Timer = Java.type("java.util.Timer");
var timer = new Timer("turnOffLaterTimer", true);

var lock = false;

var koridor = new Rule()
{
    getEventTrigger: function(){
        return [
            new ChangedEventTrigger("noolite/channel/1")
        ];
    },
    execute: function(event){
        var current = DeviceRegistry.getDeviceValue(SourceProtocol.NOOLITE, 1, "level");

        if(current.getCurrentValue() == 255 && !lock) {
            lock = true;
            print("\nLight turned ON, timer setted\n");

            // turn off past 20 minutes
            timer.schedule(function () {
                if(lock) {
                    print("\nTimes up. Turning off!\n");
                    DeviceHelper.off("noolite/channel/1")
                }
            }, 10000);
        }
        else if (current.getCurrentValue() == 255 && lock) {
            print("\nTimer already set!\n");
        }
        else if (current.getCurrentValue() == 0 && lock) {
            lock = false;
            print("\nLight turned OFF. Unset lock!\n");
        }
        else {
            //skip
        }
    }
}

// enable rules
function getRules(){
    return new RuleSet([koridor]);
}