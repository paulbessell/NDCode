/*******************************
 *
 * This Class driver the village level dynamics of ND
 *
 ******************************/
import java.util.*;
import org.apache.commons.math3.distribution.*;

public class Village {
//	Create a holder for the village's flocks
	private Flock[] vVillage;
	private int nFlocks;
/*	These variables all inherit from the population -
	they define the viruses and transmission parameters*/
	private NDInfection ndv;
	private double localTrans;
//	At present, this is defined in the constructor
	private double betweenFlockTrans;
/*	As per the Population Class,
	this is the birds available for sale and circulating ND viruses*/
	private Vector marketPool;
	private Vector ndViruses;
	private Vector ndvPool;
//	Counter for the model outputs
	private int[] ndCompartments;
	private int[] demogComps;
//	Define the vaccination parameters
	private NDInfection vaccine;
	private int vaccDay;
	private int nVaccinated;
	private int vaccFreq;

//	Overloaded constructor - defines a village with a set number of flocks
	public Village(int nFlocks){
		vVillage = new Flock[nFlocks];
		for(int i = 0; i < nFlocks; i++){
			vVillage[i] = new Flock();
		}
		this.nFlocks = nFlocks;
		this.betweenFlockTrans = 0.1;
		this.vaccFreq = 121;
	}

//	Overloaded constructor - defines a village with a sampled number of flocks - mean 50
	public Village(){
		this.nFlocks = 0;
		while(this.nFlocks == 0){
			double nFlocksD = new LogNormalDistribution(50, 20).sample();
			nFlocksD = Math.log(nFlocksD + 1);
			this.nFlocks = (int) Math.round(nFlocksD);
		}
		vVillage = new Flock[this.nFlocks];
		for(int i = 0; i < this.nFlocks; i++){
			vVillage[i] = new Flock();
		}
		this.nFlocks = nFlocks;
		this.betweenFlockTrans = 0.1;
		this.vaccFreq = 121;
	}

//	Overloaded constructor - defines a village with a sampled number of flocks - from a given mean
	public Village(int flockN, double[] parameters){
		this.nFlocks = 0;
		while(this.nFlocks == 0){
			double nFlocksD = new LogNormalDistribution(flockN, 20).sample();
			nFlocksD = Math.log(nFlocksD + 1);
			this.nFlocks = (int) Math.round(nFlocksD);
		}
		vVillage = new Flock[this.nFlocks];
		for(int i = 0; i < this.nFlocks; i++){
			vVillage[i] = new Flock(parameters, false, 1);
		}
		this.nFlocks = nFlocks;
		this.betweenFlockTrans = 0.1;
		this.vaccFreq = 121;
	}

//	MEthod to force the trojan flock into the village. The trojan flock is a pre-infected flock
	public void insertTrojan(Flock trojan){
		Flock[] replacement = new Flock[this.vVillage.length + 1];
		for(int i = 0; i < this.nFlocks; i++){
			replacement[i] = this.vVillage[i];
		}
		replacement[this.nFlocks] = trojan;
		this.vVillage = replacement;
		this.nFlocks = this.vVillage.length;
	}

	//	MEthod to force the trojan flock into the village from array
	public void insertTrojan(Flock[] trojan){
		Flock[] replacement = new Flock[this.vVillage.length + trojan.length];
		for(int i = 0; i < this.nFlocks; i++){
			replacement[i] = this.vVillage[i];
		}
		for(int i = 0; i < trojan.length; i++){
			replacement[this.nFlocks + i] = trojan[i];
		}
		this.vVillage = replacement;
		this.nFlocks = this.vVillage.length;
	}

	public Flock getTrojan(){
		return this.vVillage[this.nFlocks-1];
	}

	public Flock[] getTrojan(int n){
		Flock[] output = new Flock[n];
		for(int i = 0; i < n; i++) output[i] = this.vVillage[this.nFlocks + i - n];
		return output;
	}

	public int getnFlocks(){
		return this.nFlocks;
	}

	//	Monitor village bird demographics
	private void initDemogComps(){
		this.demogComps = new int[] {0,0,0,0};
	}
	private void updateDemogComps(Flock curr){
		this.demogComps[0] += curr.nChicksI;
		this.demogComps[1] += curr.nGrowersI;
		this.demogComps[2] += curr.nHensI;
		this.demogComps[3] += curr.nCocks;
	}

//	Method for printing flock sizes
	public void writeFlockSizes(){
		for(int i = 0; i < this.getnFlocks(); i++){
			Flock curr = this.vVillage[i];
			System.out.println("Flock size = " + curr.getSize());
		}
	}

//	Force adjustment to seasonal transmission parameters
	public void updateSeasonTransAdj(double seasonAdj){
		for(int i = 0; i < this.getnFlocks(); i++){
			Flock curr = this.vVillage[i];
			curr.setSeasonTransAdj(seasonAdj);
		}
	}

//	This is the principal method of this Class - drives the daily dynamics for each flock
	public void stepVillage(int day){
		this.initDemogComps();
		this.resetComps();
		this.resetPool();
		int fInfected = 0;
//	Loop through the flocks
		for(int i = 0; i < this.getnFlocks(); i++){
			Flock curr = this.vVillage[i];
//	Runs the time steps for the flock and extracts deaths and purchases and sales
			int[] buySell = curr.timeStep();
//	Random ND sparks
			if(this.ndvPool != null) curr.seedNDSpark(this.ndvPool, this.localTrans);
//	Run ND in the
			curr.stepND();
			if(curr.getInfected()) fInfected++;
			int[] vaccPars = {0,0,0,0,0,0,0};
				if(curr.getVaccinated()){
//	Get deaths due to ND
					int[] ndvDeaths = curr.getndDeaths();
//	An array of output parameters
					vaccPars[0] += ndvDeaths[0];
					vaccPars[1] += buySell[0];
					vaccPars[2] += buySell[1];
					vaccPars[3] += buySell[2];
					vaccPars[4] += buySell[3];
					vaccPars[5] += buySell[4];
					vaccPars[6] ++;
				}
//	Update buy-sell vectors and increment the counter
			this.incrementComps(curr.getNDComps(), curr.getndDeaths(), fInfected, curr.getSize(), buySell, vaccPars);
			this.updatePool(curr.getSalePool());
			this.updateNDPools(curr.getNDVPool());
			this.updateDemogComps(curr);
		}
		this.updateDemog();
//	Incorporate the between flock transmission
		this.betweenFlockND();
	}
	public void seedNDVector(Vector newNDV){
		this.ndvPool = newNDV;
	}
	public void seedND(NDInfection newNDV){
		this.ndv = newNDV;
	}

	public void setLocalTransmission(double trans){
		this.localTrans = trans;
	}
	public void setBetweenFlockTransmission(double trans){
		this.betweenFlockTrans = trans;
	}
	private void resetComps(){
		this.ndCompartments = new int[26];
	}
//	Run the counting method
	private void incrementComps(int[] flockND, int deaths[], int fInf, int N, int[] offTake, int[] vacc){
		ndCompartments[0] += flockND[0];
		ndCompartments[1] += flockND[1];
		ndCompartments[2] += flockND[2];
		ndCompartments[3] += flockND[3];
		ndCompartments[4] += flockND[4];
		ndCompartments[5] += deaths[0];
		ndCompartments[6] += deaths[1];
		if(flockND[2] > 0) ndCompartments[7] ++;
		ndCompartments[8] += N;
		ndCompartments[9] += offTake[0];
		ndCompartments[10] += offTake[1];
		ndCompartments[11] += offTake[2];
		ndCompartments[12] += offTake[3];
		ndCompartments[13] += offTake[4];
		ndCompartments[14] += vacc[0];
		ndCompartments[15] += vacc[1];
		ndCompartments[16] += vacc[2];
		ndCompartments[17] += vacc[3];
		ndCompartments[18] += vacc[4];
		ndCompartments[19] += vacc[5];
		ndCompartments[20] += vacc[6];
		ndCompartments[25] = this.nVaccinated;
	}
	public int[] getComps(){
		return this.ndCompartments;
	}
	public void updateDemog(){
		ndCompartments[21] += this.demogComps[0];
		ndCompartments[22] += this.demogComps[1];
		ndCompartments[23] += this.demogComps[2];
		ndCompartments[24] += this.demogComps[3];
	}
	private void resetPool(){
		this.marketPool = new Vector();
		this.ndViruses = new Vector();
	}
	private void updatePool(Vector sold){
		for(int i = 0; i < sold.size(); i++){
			this.marketPool.addElement((Bird) sold.elementAt(i));
		}
	}
	private void updateNDPools(Vector ndvs){
		for(int i = 0; i < ndvs.size(); i++){
			this.ndViruses.addElement((NDInfection) ndvs.elementAt(i));
		}
	}
	public Vector getMarketPool(){
		return this.marketPool;
	}

	public void setMarketPool(Vector vPool){
		this.marketPool = vPool;
	}

	public void updateVillagePools(){
		Collections.shuffle(this.marketPool);
		for(int i = 0; i < this.vVillage.length; i++){
			Flock curr = this.vVillage[i];
			curr.setBuyPool(this.getMarketPool());
		}
	}
//	Between flock ND transmission
	public void betweenFlockND(){
		for(int i = 0; i < this.ndViruses.size(); i++){
			NDInfection ndCurr = (NDInfection) this.ndViruses.elementAt(i);
			int infs = new BinomialDistribution(1, this.betweenFlockTrans).sample();
			if(infs == 1){
				int pos = 0;
				if(this.getnFlocks() > 1) pos = new UniformIntegerDistribution(0, this.getnFlocks()-1).sample();
				Flock curr = this.vVillage[pos];
				curr.ndBetweenFlock(ndCurr);
				curr.populateComps();
			}
		}
	}
//	Run the vaccination
	public void startVaccinate(double prop, int day, double efficacy){
		for(int i = 0; i < this.getnFlocks(); i++){
			if(Math.random() < prop) this.vVillage[i].setVaccination(day, efficacy);
		}
	}
	public void adjHenCockRatio(double ratio){
		for(int i = 0; i < this.getnFlocks(); i++){
			this.vVillage[i].adjustHenCockRatio(ratio);
		}
	}

	public void startVaccinationTrojan(int day, double efficacy){
		this.vVillage[this.nFlocks - 1].setVaccination(day + this.vaccDay, efficacy);
	}

	public void startVaccinationTrojans(int day, double efficacy, int nTrojans){
		for(int i = nTrojans; i >=1; i--) this.vVillage[this.nFlocks - i].setVaccination(day + this.vaccDay, efficacy);
	}
	public void forceND(NDInfection virus){
		this.vVillage[this.nFlocks - 1].forceND(1, virus);
	}
	public void forceND(int nSeed, NDInfection virus){
		this.vVillage[this.nFlocks - 1].forceND(nSeed, virus);
	}


	public void setVaccineDay(int day){
		this.vaccDay = day;
	}
	public void setVaccine(NDInfection vaccine){
		this.vaccine = vaccine;
	}
	public int getVaccDay(){
		return this.vaccDay;
	}
	public void vaccinate(){
		for(int i = 0; i < this.getnFlocks(); i++){
			if(this.vVillage[i].getVaccinated()){
				this.vVillage[i].vaccinate(this.vaccine, this.vaccDay);
				this.nVaccinated += vVillage[i].getSize();
			}
		}
		this.vaccDay += this.vaccFreq;
	}
	public void resetNVaccinated(){
		this.nVaccinated = 0;
	}

	public void setElasticity(double elastic){
		for(int i = 0; i < this.vVillage.length; i++){
			Flock curr = this.vVillage[i];
			curr.adjOptSize(elastic);
		}
	}
	public void setProd(double prodImp){
		for(int i = 0; i < this.vVillage.length; i++){
			Flock curr = this.vVillage[i];
			if(curr.getVaccinated()) curr.improveProductivity(prodImp);
		}
	}
	public void setVaccFreq(int freq){
		this.vaccFreq = freq;
	}
	public void setChickAge(int cAge){
		for(int i = 0; i < this.vVillage.length; i++){
			this.vVillage[i].adjustMaxChickAge(cAge);
		}
	}
	public void setGrowerAge(int cAge){
		for(int i = 0; i < this.vVillage.length; i++){
			this.vVillage[i].adjustMaxGrowerAge(cAge);
		}
	}

}
