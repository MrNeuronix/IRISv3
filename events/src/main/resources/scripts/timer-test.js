var timetask = new Rule()
{
    getEventTrigger: function () {
        return [
            new TimerTrigger("0 0 */1 * * ?")
        ];
    }
,
    execute: function (event) {
        var temp = DeviceRegistry.getDeviceValue(SourceProtocol.NOOLITE, 7, "temperature");
        log.info("Topic: {}, {}: {}", event.getTopic(), temp.getName(), temp.getCurrentValue());
    }
}

// enable rules
function getRules() {
    return new RuleSet([timetask]);
}