/************************
 * 
 * This Class implements and "manages" a group of villages in which ND is implemented
 * 
 ************************/

import java.util.Collections;
import java.util.Vector;

import org.apache.commons.math3.distribution.*;


public class Population {
//	Define the number of vilages and create a "holder"
	private final int nVillages;
	private Village[] vVillages;
//	An array to record ND statistics in the population
	private int[] ndCompartments;
//	Vectors of the birds being sold and the ND viruses circulating
	private Vector<Bird> marketPool;
	private Vector<NDInfection> ndViruses;
//	Assuming one virus is circulaitng - define here
	private NDInfection ndv;
	private double localTrans;
	private Vector<NDInfection> ndvPool;
//	Variable for the number of flocks in the population
	private int nFlocks;

//	Overloaded constructor - this method creates n villages based on generic parameters
	public Population(int nVillages){
		this.nFlocks = 0;
		this.nVillages = nVillages;
		this.vVillages = new Village[nVillages];
		for(int i = 0; i < nVillages; i++){
			this.vVillages[i] = new Village();
			this.nFlocks += vVillages[i].getnFlocks();
		}
		this.ndvPool = new Vector<NDInfection>();
	}
//	Overloaded constructor here with state specific parameters
	public Population(int nVillages, int nFlocks, double[] parameters){
		this.nFlocks = 0;
		this.nVillages = nVillages;
		this.vVillages = new Village[nVillages];
		for(int i = 0; i < nVillages; i++){
			this.vVillages[i] = new Village(nFlocks, parameters);
			this.nFlocks += vVillages[i].getnFlocks();
		}
		this.ndvPool = new Vector<NDInfection>();
	}

//	Method to force a trojan flock into the model 
	public void forceTrojan(Flock trojan){
		this.vVillages[this.vVillages.length-1].insertTrojan(trojan);
	}

	public void forceTrojan(Flock[] trojan){
		this.vVillages[this.vVillages.length-1].insertTrojan(trojan);
	}
	
	public Flock getTrojan(){
		return this.vVillages[this.vVillages.length - 1].getTrojan();
	}

	public Flock[] getTrojan(int n){
		return this.vVillages[this.vVillages.length - 1].getTrojan(n);
	}
	
	private int getnVillages(){
		return this.nVillages;
	}

//	The principal "doing" method - runs through the villages and implements the daily steps 
	public void stepPopulation(int day){
		this.resetComps();
		this.resetPool();
		for(int i = 0; i < this.getnVillages(); i++){
			Village curr = this.vVillages[i];
			curr.stepVillage(day);
			this.incrementComps(curr.getComps());
			this.updatePool(curr.getMarketPool());
		}		
	}
	
	public void resetComps(){
		this.ndCompartments = new int[28];		
	}
	
	public void resetPool(){
		this.marketPool = new Vector<Bird>();
	}
		
	public void seedND(NDInfection newNDV){
		this.ndvPool.addElement(newNDV);
	}
	
	public void setLocalTransmission(double trans){
		this.localTrans = trans;
	}

//	Clunky method to increment the counter array
	private void incrementComps(int[] flockND){
		ndCompartments[0] += flockND[0];
		ndCompartments[1] += flockND[1];
		ndCompartments[2] += flockND[2];
		ndCompartments[3] += flockND[3];
		ndCompartments[4] += flockND[4];
		ndCompartments[5] += flockND[5];
		ndCompartments[6] += flockND[6];
		ndCompartments[7] += flockND[7];
		ndCompartments[8] += flockND[8];
		ndCompartments[9] += flockND[9];
		ndCompartments[10] += flockND[10];
		ndCompartments[11] += flockND[11];
		ndCompartments[12] += flockND[12];
		ndCompartments[13] += flockND[13];
		ndCompartments[14] += flockND[14];
		ndCompartments[15] += flockND[15];
		ndCompartments[16] += flockND[16];
		ndCompartments[17] += flockND[17];
		ndCompartments[18] += flockND[18];
		ndCompartments[19] += flockND[19];
		ndCompartments[20] += flockND[20];
		ndCompartments[21] += flockND[21];
		ndCompartments[22] += flockND[22];
		ndCompartments[23] += flockND[23];
		ndCompartments[24] += flockND[24];
		ndCompartments[25] += flockND[25];
		ndCompartments[26] = this.nFlocks;
		if(flockND[1] + flockND[2] > 0) ndCompartments[27] ++;
	}
	
	public int[] getComps(){
		return this.ndCompartments;
	}

//	This adds sold birds to those available for sale
	private void updatePool(Vector sold){
		for(int i = 0; i < sold.size(); i++){
			this.marketPool.addElement((Bird) sold.elementAt(i));
		}
	}

	public Vector<Bird> getMarketPool(){
		return this.marketPool;
	}

//	Shuffles birds available for sale
	public void updateVillagePools(){
		Collections.shuffle(this.marketPool);
		for(int i = 0; i < this.getnVillages(); i++){
			Village curr = this.vVillages[i];
			curr.setMarketPool(this.marketPool);
		}
	}

//	Overloaded methods to set the ND viruses depending upon the mparameters being (re)defined	
	public void setNDVs(double localTrans){
		for(int i = 0; i < this.getnVillages(); i++){
			this.vVillages[i].seedNDVector(this.ndvPool);
			this.vVillages[i].setLocalTransmission(localTrans);
		}
	}

	public void setNDVs(double localTrans, double betweenFlock, double seasonT){
		for(int i = 0; i < this.getnVillages(); i++){
			this.vVillages[i].seedNDVector(this.ndvPool);
			this.vVillages[i].setLocalTransmission(localTrans);
			this.vVillages[i].setBetweenFlockTransmission(betweenFlock);
			this.vVillages[i].updateSeasonTransAdj(seasonT);
		}
	}
	
//	Define the vaccination regime	
	public void startVaccination(NDInfection vaccine, double prop, int sDay, double efficacy, int frequency){
		for(int i = 0; i < this.getnVillages(); i++){
			this.vVillages[i].setVaccine(vaccine);
			int day = new UniformIntegerDistribution(1,90).sample();
			this.vVillages[i].setVaccineDay(sDay + day);
			this.vVillages[i].startVaccinate(prop, sDay + day, efficacy);
			this.vVillages[i].setVaccFreq(frequency);
		}
	}
	public void startVaccinationFixed(NDInfection vaccine, double prop, int sDay, double efficacy, int frequency){
		for(int i = 0; i < this.getnVillages(); i++){
			this.vVillages[i].setVaccine(vaccine);
			this.vVillages[i].setVaccineDay(sDay + 1);
			this.vVillages[i].startVaccinate(prop, sDay + 1, efficacy);
			this.vVillages[i].setVaccFreq(frequency);
		}
	}

	public void startVaccinationTrojan(NDInfection vaccine, int sDay, double efficacy){
			this.vVillages[this.vVillages.length-1].startVaccinationTrojan(sDay, efficacy);
	}
	public void startVaccinationTrojans(NDInfection vaccine, int sDay, double efficacy, int nTrojans){
		this.vVillages[this.vVillages.length-1].startVaccinationTrojans(sDay, efficacy, nTrojans);
}
	
	public void startVaccinationTrojanSolo(NDInfection vaccine, int sDay, double efficacy, int frequency){	
		this.vVillages[this.vVillages.length - 1].setVaccine(vaccine);
		this.vVillages[this.vVillages.length - 1].startVaccinationTrojan(sDay, efficacy);
		int day = new UniformIntegerDistribution(1, 90).sample();
		this.vVillages[this.vVillages.length - 1].setVaccineDay(sDay + day);
		this.vVillages[this.vVillages.length - 1].setVaccFreq(frequency);
	}

//	Run each day to vaccinate flocks that are due vaccination	
	public void vaccinate(int day){
		for(int i = 0; i < this.getnVillages(); i++){
			vVillages[i].resetNVaccinated();
			if(vVillages[i].getVaccDay() == day){
				vVillages[i].vaccinate();
			}
		}
	}
	
	public void ndTrojan(NDInfection virus){
		this.vVillages[this.vVillages.length - 1].forceND(virus);		
	}
	public void ndTrojan(int nSeeds, NDInfection virus){
		this.vVillages[this.vVillages.length - 1].forceND(nSeeds, virus);		
	}

	public void setElasticity(double elastic){
		for(int i = 0; i < this.getnVillages(); i++) vVillages[i].setElasticity(elastic);		
	}
	
	public void setProdImprovement(double imp){
		for(int i = 0; i < this.getnVillages(); i++) vVillages[i].setProd(imp);		
	}
	public void setHenCockRatio(double ratio){
		for(int i = 0; i < this.getnVillages(); i++) this.vVillages[i].seedNDVector(this.ndvPool);
	}
	public void setMaxChickAge(int cAge){
		for(int i = 0; i < this.getnVillages(); i++) this.vVillages[i].setChickAge(cAge);
	}
	public void setMaxGrowerAge(int cAge){
		for(int i = 0; i < this.getnVillages(); i++) this.vVillages[i].setGrowerAge(cAge);
	}
	
}
