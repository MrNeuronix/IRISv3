'use strict';

print("\n################# E X A M P L E S ##################\n");

var timetask = new Rule(){
    getEventTrigger: function(){
        return [
            new TimerTrigger("0/15 * * * * ?")
        ];
    },
    execute: function(event){
        print("\nTimerTest\n");
    }
};

// enable rules
function getRules(){return new RuleSet([timetask]);}