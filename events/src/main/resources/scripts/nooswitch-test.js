var Timer = Java.type("java.util.Timer");
var timer = new Timer("turnOffLaterTimer", true);

var lock = false;

var autoOff = new Rule()
{
    getEventTrigger: function () {
        return [
            new ChangedEventTrigger("noolite/channel/1"),
            new ChangedEventTrigger("noolite/channel/2"),
            new ChangedEventTrigger("noolite/channel/4")
        ];
    }
,
    execute: function (event) {
        var current = DeviceRegistry.getDeviceValue(event.getDevice().getHumanReadableName(), "level");

        if (current == null)
            return;

        if (current.getCurrentValue() == 255 && !lock) {
            lock = true;
            print("\nLight turned ON on channel " + event.getDevice().getChannel() + ", timer setted\n");

            // turn off past 20 minutes
            timer.schedule(function () {
                if (lock) {
                    print("\nTimes up. Turning off channel " + event.getDevice().getChannel() + "!\n");
                    DeviceHelper.off(event.getDevice().getHumanReadableName())
                }
            }, 1200000);
        }
        else if (current.getCurrentValue() == 255 && lock) {
            print("\nTimer already set!\n");
        }
        else if (current.getCurrentValue() == 0 && lock) {
            lock = false;
            print("\nLight turned OFF on channel " + event.getDevice().getChannel() + ". Unset lock!\n");
        }
        else {
            //skip
        }
    }
}

// enable rules
function getRules() {
    return new RuleSet([autoOff]);
}