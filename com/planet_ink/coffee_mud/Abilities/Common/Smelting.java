package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Smelting extends CraftingSkill
{
	public String ID() { return "Smelting"; }
	public String name(){ return "Smelting";}
	private static final String[] triggerStrings = {"SMELT","SMELTING"};
	public String[] triggerStrings(){return triggerStrings;}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD_ALWAYSONEONE=3;
	private static final int RCP_VALUE_DONTMATTER=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_METALONE=6;
	private static final int RCP_METALTWO=7;

	private Item building=null;
	private Item fire=null;
	private boolean messedUp=false;
	private int amountMaking=0;
	private static boolean mapped=false;
	public Smelting()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if((building==null)
			||(fire==null)
			||(amountMaking<1)
			||(!Sense.isOnFire(fire))
			||(!mob.location().isContent(fire))
			||(mob.isMine(fire)))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("SMELTING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"smelting.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Smelting","Recipes not found!");
			Resources.submitResource("SMELTING RECIPES",V);
		}
		return V;
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					amountMaking=amountMaking*(abilityCode());
					if(messedUp)
						commonEmote(mob,"<S-NAME> ruin(s) "+building.name()+"!");
					else
					for(int i=0;i<amountMaking;i++)
					{
						Item copy=(Item)building.copyOf();
						copy.setMiscText(building.text());
						copy.recoverEnvStats();
						mob.location().addItemRefuse(copy,Item.REFUSE_PLAYER_DROP);
					}
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		randomRecipeFix(mob,loadRecipes(),commands,0);
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"smelt list\" for a list.");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" "+Util.padRight("Metal #1",20)+" Metal #2\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					String metal1=((String)V.elementAt(this.RCP_METALONE)).toLowerCase();
					String metal2=((String)V.elementAt(this.RCP_METALTWO)).toLowerCase();
					if(level<=mob.envStats().level())
						buf.append(Util.padRight(item,20)+" "+Util.padRight(metal1,20)+" "+metal2+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		fire=getRequiredFire(mob,0);
		if(fire==null) return false;
		building=null;
		messedUp=false;
		String recipeName=Util.combine(commands,0);
		int maxAmount=0;
		if((commands.size()>1)&&(Util.isNumber((String)commands.lastElement())))
		{
			maxAmount=Util.s_int((String)commands.lastElement());
			commands.removeElementAt(commands.size()-1);
			recipeName=Util.combine(commands,0);
		}
		Vector foundRecipe=null;
		Vector matches=matchingRecipeNames(recipes,recipeName);
		for(int r=0;r<matches.size();r++)
		{
			Vector V=(Vector)matches.elementAt(r);
			if(V.size()>0)
			{
				int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
				if(level<=mob.envStats().level())
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,"You don't know how to make '"+recipeName+"'.  Try \"smelt list\" for a list.");
			return false;
		}
		String doneResourceDesc=(String)foundRecipe.elementAt(RCP_FINALNAME);
		String resourceDesc1=(String)foundRecipe.elementAt(RCP_METALONE);
		String resourceDesc2=(String)foundRecipe.elementAt(RCP_METALTWO);
		int resourceCode1=-1;
		int resourceCode2=-1;
		int doneResourceCode=-1;
		for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
		{
			String desc=EnvResource.RESOURCE_DESCS[i];
			if(desc.equalsIgnoreCase(resourceDesc1))
				resourceCode1=i;
			if(desc.equalsIgnoreCase(resourceDesc2))
				resourceCode2=i;
			if(desc.equalsIgnoreCase(doneResourceDesc))
				doneResourceCode=i;
		}
		if((resourceCode1<0)||(resourceCode2<0)||(doneResourceCode<0))
		{
			commonTell(mob,"CoffeeMud error in this alloy.  Please let your local Archon know.");
			return false;
		}
		int amountResource1=findNumberOfResource(mob.location(),EnvResource.RESOURCE_DATA[resourceCode1][0]);
		int amountResource2=findNumberOfResource(mob.location(),EnvResource.RESOURCE_DATA[resourceCode2][0]);
		if(amountResource1==0)
		{
			commonTell(mob,"There is no "+resourceDesc1+" here to make "+doneResourceDesc+" from.  It might need to put it down first.");
			return false;
		}
		if(amountResource2==0)
		{
			commonTell(mob,"There is no "+resourceDesc2+" here to make "+doneResourceDesc+" from.  It might need to put it down first.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		amountMaking=amountResource1;
		if(amountResource2<amountResource1) amountMaking=amountResource2;
		if((maxAmount>0)&&(amountMaking>maxAmount)) amountMaking=maxAmount;
		destroyResources(mob.location(),amountMaking,EnvResource.RESOURCE_DATA[resourceCode1][0],0,null,0);
		destroyResources(mob.location(),amountMaking,EnvResource.RESOURCE_DATA[resourceCode2][0],0,null,0);
		completion=Util.s_int((String)foundRecipe.elementAt(this.RCP_TICKS))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
		amountMaking+=amountMaking;
		building=(Item)CoffeeUtensils.makeResource(EnvResource.RESOURCE_DATA[doneResourceCode][0],-1,false);
		startStr="<S-NAME> start(s) smelting "+doneResourceDesc.toLowerCase()+".";
		displayText="You are smelting "+doneResourceDesc.toLowerCase();
		verb="smelting "+doneResourceDesc.toLowerCase();

		messedUp=!profficiencyCheck(mob,0,auto);
		if(completion<4) completion=4;

		FullMsg msg=new FullMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
