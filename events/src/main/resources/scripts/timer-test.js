var timetask = new Rule()
{
    getEventTrigger: function () {
        return [
            new TimerTrigger("0 0 */1 * * ?")
        ];
    }
,
    execute: function (event) {
        print("\nTimerTest\n");
        var temp = DeviceRegistry.getDeviceValue(SourceProtocol.NOOLITE, 7, "temperature");
        print("\n" + temp.getName() + ": " + temp.getCurrentValue() + "\n");
    }
}

// enable rules
function getRules() {
    return new RuleSet([timetask]);
}