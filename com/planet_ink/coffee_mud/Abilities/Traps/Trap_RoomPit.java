package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_RoomPit extends StdTrap
{
	public String ID() { return "Trap_RoomPit"; }
	public String name(){ return "pit trap";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 1;}
	public String requiresToSet(){return "";}
	public Environmental newInstance(){	return new Trap_RoomPit();}
	protected Vector pit=null;
	
	public void unInvoke()
	{
		if((pit!=null)
		&&(canBeUninvoked())
		&&(pit.size()>1))
		{
			Room R1=(Room)pit.firstElement();
			Room R2=(Room)pit.lastElement();
			while(R1.numInhabitants()>0)
			{
				MOB M=R1.fetchInhabitant(0);
				if(M!=null){
					M.killMeDead(false);
					R1.delInhabitant(M);
				}
			}
			while(R2.numInhabitants()>0)
			{
				MOB M=R2.fetchInhabitant(0);
				if(M!=null){
					M.killMeDead(false);
					R2.delInhabitant(M);
				}
			}
			Room R=R2.getRoomInDir(Directions.UP);
			if((R!=null)&&(R.getRoomInDir(Directions.DOWN)==R2))
			{
				R.rawDoors()[Directions.DOWN]=null;
				R.rawExits()[Directions.DOWN]=null;
			}
			R2.rawDoors()[Directions.UP]=null;
			R2.rawExits()[Directions.UP]=null;
			R2.rawDoors()[Directions.DOWN]=null;
			R2.rawExits()[Directions.DOWN]=null;
			R1.rawDoors()[Directions.UP]=null;
			R1.rawExits()[Directions.UP]=null;
			pit=null;
			super.unInvoke();
		}
		else
		{
			pit=null;
			super.unInvoke();
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;
		
		if((tickID==Host.TRAP_DESTRUCTION)
		&&(canBeUninvoked())
		&&(pit!=null)
		&&(pit.size()>1)
		&&(((((Room)pit.firstElement()).numPCInhabitants()>0)
			||(((Room)pit.lastElement()).numPCInhabitants()>0))))
			return true;
		else
			return super.tick(ticking,tickID);
	}
	
	protected synchronized void makePit(MOB target)
	{
		if((pit==null)||(pit.size()<2))
		{
			Vector V=new Vector();
			Room myPitUp=CMClass.getLocale("ClimbableSurface");
			myPitUp.setArea(target.location().getArea());
			myPitUp.baseEnvStats().setDisposition(myPitUp.baseEnvStats().disposition()|EnvStats.IS_DARK);
			myPitUp.setDisplayText("Inside a dark pit");
			myPitUp.setDescription("The walls here are slick and tall.  The trap door has already closed.");
			myPitUp.recoverEnvStats();

			Room myPit=CMClass.getLocale("StdRoom");
			myPit.setArea(target.location().getArea());
			myPit.baseEnvStats().setDisposition(myPit.baseEnvStats().disposition()|EnvStats.IS_DARK);
			myPit.setDisplayText("Inside a dark pit");
			myPit.setDescription("The walls here are slick and tall.  You can barely see the closed trap door well above you.");
			myPit.rawExits()[Directions.UP]=CMClass.getExit("StdOpenDoorway");
			myPit.rawDoors()[Directions.UP]=myPitUp;
			myPitUp.rawExits()[Directions.DOWN]=CMClass.getExit("StdOpenDoorway");
			myPitUp.rawDoors()[Directions.DOWN]=myPit;
			myPitUp.recoverEnvStats();
			V.addElement(myPit);
			V.addElement(myPitUp);
			pit=V;
		}
	}
	
	public void finishSpringing(MOB target)
	{
		if(target.envStats().weight()<5)
			target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> float(s) gently into the pit!");
		else
		{
			target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> hit(s) the pit floor with a THUMP!");
			int damage=Dice.roll(trapLevel(),6,1);
			ExternalPlay.postDamage(invoker(),target,this,damage,Affect.NO_EFFECT,-1,null);
		}
		ExternalPlay.look(target,null,true);
	}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null)&&(!Sense.isInFlight(target)))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) falling into a pit!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> fall(s) into a pit!"))
			{
				super.spring(target);
				makePit(target);
				((Room)pit.lastElement()).rawExits()[Directions.UP]=CMClass.getExit("StdClosedDoorway");
				((Room)pit.lastElement()).rawDoors()[Directions.UP]=target.location();
				if((target.location().getRoomInDir(Directions.DOWN)==null)
				&&(target.location().getExitInDir(Directions.DOWN)==null))
				{
					target.location().rawExits()[Directions.DOWN]=CMClass.getExit("StdClosedDoorway");
					target.location().rawDoors()[Directions.DOWN]=((Room)pit.lastElement());
				}
				((Room)pit.firstElement()).bringMobHere(target,false);
				finishSpringing(target);
			}
		}
	}
}
