package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_ImprovedDistraction extends ThiefSkill
{
	public String ID() { return "Thief_ImprovedDistraction"; }
	public String name(){ return "Improved Distraction";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Thief_Distract")==null)
		{
			teacher.tell(student.name()+" has not yet learned to distract.");
			student.tell("You need to learn to distract first.");
			return false;
		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.tool()!=null)
		&&(msg.tool().ID().equals("Thief_Distract")))
		{
			helpProfficiency(mob);
			Ability A=mob.fetchAbility("Thief_Distract");
			A.setAbilityCode(profficiency()/5);
		}
		return super.okMessage(myHost,msg);
	}
}
