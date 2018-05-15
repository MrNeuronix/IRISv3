var runtask = new Rule()
{
    getEventTrigger: function () {
        return [
            new RunCommandTrigger("testCommand")
        ];
    }
,
    execute: function (event) {
        log.info("Run testCommand command. Topic: {}", event.getTopic());
    }
}

// enable rules
function getRules() {
    return new RuleSet([runtask]);
}