/**********************************
 * 
 * This Class runs the flock dynamics
 * 
 *********************************/

import java.util.*;

import org.apache.commons.math3.distribution.*;

public class Flock {
	// Holder for the birds
	private Vector<Bird> vFlock;
	// Numbers by category
	private int flockSize;
	public int nChicksI;
	public int nGrowersI;
	public int nHensI;
	public int nCocks;
	// Variables
	private final double chickDeath;
	private final double growerDeath;
	private final double adultDeath;
	private final double birthRate;
	private double hatchRate;
	private final double eggRate;
	private final double gSaleRate;
	private final double aSaleRate;
	private double henCockRatio;
	private final double propHen;
	private final double buyRate;
	private final double season;
	// Fixed parameters
	private double optSize;
	private double adjFactor;
	private boolean chickAgeRamp;
	private boolean infection;
	private double seasonTransAdj;
	// Season adjustments
	private boolean seasonal;
	private double chickDeathA;
	private double growerDeathA;
	private double adultDeathA;
	// Helper buckets
	private int[] ndCompartments;
	private int[] ndCompartmentsOut;
	private Vector<Bird> salePool;
	private Vector buyPool;
	private Vector<NDInfection> ndViruses;
	// Counters and markers
	private int ndDeaths;
	private int ndDeathsChicks;
	private int ndDeathsGrowers;
	private int ndDeathsHens;
	private int ndDeathsCocks;
	private int deathsChicks;
	private int deathsGrowers;
	private int deathsHens;
	private int deathsCocks;
	private int births;
	private int eggsDay;
	private int[] saleOutput;
	private int graduateChick;
	private int graduateGrowerH;
	private int graduateGrowerC;
	private int clutchDay;
	private int deadNotVacc;
	private int deadFail;
	private int deadExpire;
	private int cProtected;
	private int gProtected;
	private int aProtected;
	// Vaccination parameters
	private boolean isVaccinated;
	private int vaccDay;
	private double vEfficacy;
	private boolean vaccineFail;
	private int maxChickAge;
	private int maxGrowerAge;
	private int cumSales;

/*	Overloaded constructor based on Myanmar data that creates flocks of sizes 
	defined by the parameters for Myanmar */	
	public Flock(){
		this.vFlock = new Vector<Bird>();
		double nChicks = new LogNormalDistribution(12.1, 7.8).sample();
		nChicks = Math.log(nChicks + 1);
		this.nChicksI = (int) Math.round(nChicks);
		for(int i = 0; i < nChicksI; i++) this.vFlock.addElement(new Bird("Chick"));

		double nGrowers = new LogNormalDistribution(12.6, 7.9).sample();
		nGrowers = Math.log(nGrowers + 1);
		this.nGrowersI = (int) Math.round(nGrowers);
		for(int i = 0; i < nGrowersI; i++) this.vFlock.addElement(new Bird("Grower"));

		double nHens = new LogNormalDistribution(4, 3.1).sample();
		nHens = Math.log(nHens + 1);
		this.nHensI = (int) Math.round(nHens);
		for(int i = 0; i < nHensI; i++) this.vFlock.addElement(new Bird("Hen"));

		this.nCocks = new PoissonDistribution(1.6).sample();
		for(int i = 0; i < nCocks; i++) this.vFlock.addElement(new Bird("Cock"));
		
		this.flockSize = nChicksI + nGrowersI + nHensI + nCocks;
//	Predefined flock parameters for Myanmar		
		this.chickDeath = 0.01378;
		this.growerDeath = 0.01;
		this.adultDeath = 0.00136;
		this.birthRate = 2.9/365;
		this.hatchRate = 9.7;
		this.eggRate = 0;
		this.gSaleRate = 0.0065;
		this.aSaleRate = 0.001;
		this.henCockRatio = 2.5;
		this.propHen = 4/5.6;
		this.buyRate = 0.00045 * 2;
		this.season = 0.4;
		this.chickDeathA = this.chickDeath;
		this.growerDeathA = this.growerDeath;
		this.adultDeathA = this.adultDeath;
		this.buyPool = new Vector();
		
// Initialise the buy sale adjustment factors		
		this.optSize = (double) this.flockSize + (double)((30-this.flockSize) / 2); 
		this.getAdjFactor();
		this.seasonTransAdj = 1;
		this.maxChickAge = 42;
		this.maxGrowerAge = 150;
	}

	/* Overloaded constructor that implements a flock of szes determined by an 
	  array of given parameters. 
	  Forced is for the trojan flocks - imposes a fixed size on these flocks */
	public Flock(double[] params, boolean forced, double scale){
		this.vFlock = new Vector<Bird>();

		this.nChicksI = 0;
		this.nGrowersI = 0;
		
		int nChickens = new PoissonDistribution(params[0]).sample();
		for(int i = 0; i < nChickens; i++) {
			double prop = 0.5;
			boolean bType = Math.random() < prop;
			if(bType) {
				this.vFlock.addElement(new Bird("Chick"));
				this.nChicksI ++;
			}
			else {
				this.vFlock.addElement(new Bird("Grower"));
				this.nGrowersI ++;
			}
		}

		this.nHensI = new PoissonDistribution(params[1]).sample();
		for(int i = 0; i < nHensI; i++) this.vFlock.addElement(new Bird("Hen"));

		this.nCocks = new PoissonDistribution(params[2]).sample();
		for(int i = 0; i < nCocks; i++) this.vFlock.addElement(new Bird("Cock"));
		
		this.flockSize = nChicksI + nGrowersI + nHensI + nCocks;
//	Define flock parameters from the array of parameters.		
		this.chickDeath = params[3];
		this.growerDeath = params[4];
		this.adultDeath = params[5];
		this.birthRate = params[6] / 365;
		this.hatchRate = params[7];
		this.eggRate = params[8];
		this.gSaleRate = params[9];
		this.aSaleRate = params[10];
		this.buyRate = params[11];
		
		this.henCockRatio = 2.5;
		this.propHen = params[1] / (params[1] + params[2]);
		this.season = 0.4;
	//Seasonal adjustment parameters
		this.chickDeathA = this.chickDeath;
		this.growerDeathA = this.growerDeath;
		this.adultDeathA = this.adultDeath;
		this.buyPool = new Vector();
		
		double baseSize = params[0] + params[1] + params[2];
// Initialise the buy sale adjustment factors		
		if(!forced) this.optSize = (double) this.flockSize + (double)((baseSize - this.flockSize)) / 2;
		else this.optSize = baseSize * scale;
		this.getAdjFactor();
		this.chickAgeRamp = true;
		this.seasonTransAdj = 1;
		this.maxChickAge = 42;
		this.maxGrowerAge = 150;
		this.cumSales = 0;
	}
	
	public void setSeasonTransAdj(double tAdj){
		this.seasonTransAdj = tAdj;
	}
	
	public boolean getInfected(){
		return this.infection;
	}
//	Returns various outputs of flock indicators
	public int[] getndDeaths(){
		return new int[] {this.ndDeaths, this.ndDeathsChicks};
	}
	public int[] getndDeathsReason(){
		return new int[] {this.deadNotVacc, this.deadFail, this.deadExpire};
	}	
	public int[] getndDeathsAge(){
		return new int[] {this.ndDeathsChicks, this.ndDeathsGrowers, this.ndDeathsHens, this.ndDeathsCocks};
	}
	public int[] getDeathsAge(){
		return new int[] {this.deathsChicks, this.deathsGrowers, this.deathsHens, this.deathsCocks};
	}
	public int getTotalDeaths(){
		return this.deathsChicks + this.deathsGrowers + this.deathsHens + this.deathsCocks;
	}
	public int[] getGraduates(){
		return new int[] {this.graduateChick, this.graduateGrowerH, this.graduateGrowerC};
	}
	public int[] getProtected(){
		return new int[] {this.cProtected, this.gProtected, this.aProtected};	
	}
	public int getBirths(){
		return this.births;
	}
	public int getNewGrowers(){
		return this.graduateChick;
	}

	public Vector<Bird> getSalePool(){
		return this.salePool;
	}
	public Vector<NDInfection> getNDVPool(){
		return this.ndViruses;
	}
	public void setBuyPool(Vector salePool){
		this.buyPool = salePool;
	}
	public int[] getSaleOutput(){
		return this.saleOutput;
	}
	public int getVaccDay(){
		return this.vaccDay;
	}
	
	public int getCumSales(){
		return this.cumSales;
	}
	
	public void resetCumSales(){
		this.cumSales = 0;
	}

//	Seasonal adjustment to flock death rates - Myanmar model
	public void getSeasonAdj(int day){
		double seasonA = 1;
		double year = Math.floor((double) day / 365);
		double dayR = ((double) day / 365) - year;
		if(dayR <= 0.75){
			seasonA = 1- this.season;
		}
		if(dayR > 0.75){
			seasonA = 1 + (this.season / 3);
		}
		this.chickDeathA = this.chickDeath * seasonA;
		this.growerDeathA = this.growerDeath * seasonA;
		this.adultDeathA = this.adultDeath * seasonA;		
	}

// Principal method that implements the flock dynamics
	public int[] timeStep(){
		this.deathsChicks = 0;
		this.deathsGrowers = 0;
		this.deathsHens = 0;
		this.deathsCocks = 0;
		this.cProtected = 0;
		this.gProtected = 0;
		this.aProtected = 0;
		this.graduateChick = 0;
		this.graduateGrowerH = 0;
		this.graduateGrowerC = 0;
		this.resetCount();	
//	Loop through all the birds in the flock
		for(int i = 0; i < this.vFlock.size(); i++){
			Bird currBird = this.vFlock.elementAt(i);
//	This deals with bird deaths and adjusts the connters accordingly
			boolean dead = this.birdDeath(currBird);
			if(!dead){
				currBird.incrementAge(this.propHen);
				if(currBird.getType() == "Chick") this.nChicksI ++;
				if(currBird.getType() == "Grower") this.nGrowersI ++;
				if(currBird.getType() == "Hen") this.nHensI ++;
				if(currBird.getType() == "Cock") this.nCocks ++;

				if(currBird.getType() == "Chick" & currBird.testProtected()) this.cProtected ++;
				if(currBird.getType() == "Grower" & currBird.testProtected()) this.gProtected ++;
				if(currBird.getType() == "Hen" & currBird.testProtected()) this.aProtected ++;
				if(currBird.getType() == "Cock" & currBird.testProtected()) this.aProtected ++;

				if(currBird.getAge() == this.maxChickAge) this.graduateChick ++;
				if(currBird.getAge() == this.maxGrowerAge & currBird.getType() == "Hen") this.graduateGrowerH ++;
				if(currBird.getAge() == this.maxGrowerAge & currBird.getType() == "Cock") this.graduateGrowerC ++;

			}
			if(dead){
				if(currBird.getType() == "Chick") this.deathsChicks ++;
				if(currBird.getType() == "Grower") this.deathsGrowers ++;
				if(currBird.getType() == "Hen") this.deathsHens ++;
				if(currBird.getType() == "Cock") this.deathsCocks ++;
				this.vFlock.removeElementAt(i);
				i--;
			}
		}
//	Bird births
		this.birdBirths();
		this.flockCount();
		this.getAdjFactor();
		int gsales = this.growerSales();
		int asales = this.adultSales();
		int buys = this.buyBirdPool();
		int[] output = {gsales, asales, buys, this.clutchDay, this.eggsDay};
		this.saleOutput = output;
		this.cumSales = this.cumSales + gsales + asales;
		return output;
	}

//	Determine whether a bird dies that day
	public boolean birdDeath(Bird cBird){
		boolean dead = false;
		double cDeath = this.chickDeathA;
		if(this.chickAgeRamp) cDeath = (this.chickDeathA * ((this.maxChickAge - (double) cBird.getAge()) / this.maxChickAge)) + this.growerDeathA;
		if(cBird.getType() == "Chick") dead = Math.random() < cDeath;
		if(cBird.getType() == "Grower") dead = Math.random() < this.growerDeathA;
		if(cBird.getType() == "Hen") dead = Math.random() < this.adultDeathA;
		if(cBird.getType() == "Cock") dead = Math.random() < this.adultDeathA;
		
		return dead;
	}

//	Determine whether a hen hatches a clutch of eggs that day and adds them to the flock
	public void birdBirths(){
		this.eggsDay = 0;
		this.clutchDay = 0;
		this.births = 0;
		int nClutch = new BinomialDistribution(this.nHensI, this.birthRate).sample();  
		this.clutchDay += nClutch;
		for(int i = 1; i <= nClutch; i++){
			int newBirds = new PoissonDistribution(this.hatchRate).sample();
			this.addBirths(newBirds);
			this.eggsDay += new PoissonDistribution(this.eggRate).sample();
			this.births += newBirds;
		}
	}
	
	private void addBirths(int nBirths){
		for(int i = 1; i <= nBirths; i++){
			this.vFlock.addElement(new Bird(this.maxChickAge, this.maxGrowerAge));
			this.nChicksI++;
		}
	}

//	Several methods that deal with grower sales by age of the birds
	private int growerSales(){
		int gSale = new BinomialDistribution(this.nGrowersI, this.gSaleRate * this.adjFactor).sample();
		for(int i = 1; i <= gSale; i++){
			int[] ageArray = this.growerAges();
			double[] ageProb = this.growerAgesProbs(ageArray);
			int sampledAge = new EnumeratedIntegerDistribution(ageArray, ageProb).sample();
			this.removeSelectedBird(sampledAge);
			this.nGrowersI --;
		}
		return gSale;
	}
	private int growerSalesMax(){
		int gSale = new BinomialDistribution(this.nGrowersI, this.gSaleRate * this.adjFactor).sample();
		for(int i = 1; i <= gSale; i++){
			int[] ageArray = this.growerAges();
			this.removeOldestGrower();
			this.nGrowersI --;
		}
		return gSale;
	}
	
	private void removeOldestGrower(){
		int pos = 0;
		int mAge = 0;
		for(int i = 0; i < this.vFlock.size(); i++){
			Bird curr = this.vFlock.elementAt(i);
			if(curr.getType() == "Grower" && curr.getAge() > mAge){
				mAge = curr.getAge();
				pos = i;
			}
		}
		this.vFlock.removeElementAt(pos);
	}

//	Simple method to get the ages of growers in the flock	
	private int[] growerAges(){
		int[] gAges = new int[this.nGrowersI];
		int pos = 0;
		for(int i = 0; i < this.vFlock.size(); i++){
			Bird curr = this.vFlock.elementAt(i);
			if(curr.getType() == "Grower"){
				gAges[pos] = curr.getAge();
				pos++;
			}
		}
		return gAges;
	}

	//	Assigns probabilities of growers being sold base don their ages.
	private double[] growerAgesProbs(int[] ageArray){
		double[] gAgeP = new double[this.nGrowersI];
		double gAgeSum = 0;
		for(int i = 0; i < ageArray.length; i++){
			gAgeSum += Math.pow((double) ageArray[i], 2);
			gAgeP[i] = Math.pow((double) ageArray[i], 2);
		}
		for(int i = 0; i < gAgeP.length; i++){
			gAgeP[i] = gAgeP[i] / gAgeSum;
		}		
		return gAgeP;
	}

//	Remove a nominated bird identified by its age - slight bodge, but works fine 	
	private void removeSelectedBird(int bAge){
		boolean match = false;
		int count = 0;
		while(!match){
			Bird currBird = this.vFlock.elementAt(count);
			match = currBird.getAge() == bAge;
			if(match) this.vFlock.removeElementAt(count);
			count ++;
		}
	}

	//	Determines the sales of adult birds depending on the unmber of hens and cocks
	private int adultSales(){
		this.salePool = new Vector<Bird>();
		int nASold = new BinomialDistribution(this.nCocks + this.nHensI, this.aSaleRate * this.adjFactor).sample();
		for(int i = 1; i <= nASold; i++){
			int sCock = this.nCocks;
			int sHen = this.nHensI;
			if(sCock > 0 && sHen > 0){
				if((double) this.nHensI / (double) this.nCocks > this.henCockRatio) this.removeHen();
				else this.removeCock();
			}
			if(sCock == 0) this.removeHen();
			if(sHen == 0) this.removeCock();
		}
		return nASold;
	}
//	Two methods that remove a hen or a cock - first one encontered
	private void removeHen(){
		boolean flag = false;
		int count = 0;
		while(!flag){
			Bird curr = this.vFlock.elementAt(count);
			if(curr.getType() == "Hen"){
				flag = true;
				this.salePool.addElement(this.vFlock.elementAt(count));
				this.vFlock.removeElementAt(count);
				this.nHensI --;
			}
			count ++;
		}		
	}
	
	private void removeCock(){
		boolean flag = false;
		int count = 0;
		while(!flag){
			Bird curr = this.vFlock.elementAt(count);
			if(curr.getType() == "Cock"){
				flag = true;
				this.salePool.addElement(this.vFlock.elementAt(count));
				this.vFlock.removeElementAt(count);
				this.nCocks --;
			}
			count ++;
		}		
	}

//	Buy in birds by creating new birds	
	private int buyBird(){
//	Default value for when there are no birds in the flock
		double buyProb = 0.05;
//	If there are birds in the flock then adjust the buy rate based on optimal flocks ize
		if(this.getSize() > 0) buyProb = this.buyRate / this.adjFactor;
		int bought = new BinomialDistribution(1, buyProb).sample();
		if(bought == 1){
			boolean female = (double) this.nHensI / (double) this.nCocks < this.henCockRatio;
			Bird newBird = new Bird(female);
			this.addBird(newBird);
		}
		return bought;
	}

//	As above but buy birds from a pool of birds	
	private int buyBirdPool(){
		double buyProb = 0.01;
		if(this.getSize() > 0) buyProb = this.buyRate / this.adjFactor;
		if(buyProb > 0.01) buyProb = 0.01;
		int bought = new BinomialDistribution(1, buyProb).sample();
		boolean flag = false;
		if(bought == 1){
			boolean female = (double) this.nHensI / (double) this.nCocks < this.henCockRatio;
			for(int i = 0; i < this.buyPool.size(); i++){
				while(!flag){
					Bird currBird = (Bird) this.buyPool.elementAt(i);
					if((currBird.getType() == "Hen") == female){
						this.addBird(currBird);
						this.buyPool.removeElementAt(i);
						i--;
						flag = true;
					}
					if((currBird.getType() == "Cock") == !female){
						this.addBird(currBird);
						this.buyPool.removeElementAt(i);
						i--;
						flag = true;
					}					
				}
			}
			if(!flag){
				Bird newBird = new Bird(female);
				this.addBird(newBird);
			}
		}
		return bought;
	}

//	Add a purchased bird to the flock
	private void addBird(Bird newBird){
		this.vFlock.addElement(newBird);
		if(newBird.getType() == "Cock") this.nCocks ++;
		if(newBird.getType() == "Hen") this.nHensI ++;		
	}
	
	private void resetCount(){
		this.nChicksI = 0;
		this.nCocks = 0;
		this.nGrowersI = 0;
		this.nHensI = 0;
	}
	
	private void flockCount(){
		this.flockSize = this.nChicksI + this.nGrowersI + this.nHensI + this.nCocks;
	}
	public int getSize(){
		return this.vFlock.size();
	}

	//	Selling adjustment factor for adults
	private void getAdjFactor(){
		this.adjFactor = 1 - (1 - ((double) this.flockSize / this.optSize));
		if(this.nHensI == 0) this.adjFactor = 0.0001;
	}
	//	Selling adjustment factor for growers
	private void getAdjFactorG(){
		double nNotChick = (double) this.nGrowersI + this.nHensI + this.nCocks;
		this.adjFactor = 1 - (1 - (nNotChick / this.optSize));
	}

/*************************
 * 
 * Code below deals with Newcastle disease in the flock
 * 
 */
/*	Seed Newcastle disease in the flock - defines the number of birds to seed
 * used for the single flock instances */
	public void seedND(int nSeed, NDInfection ndVirus){
		for(int i = 1; i <= nSeed; i++){
			int pos = 0;
			if(this.vFlock.size() > 1) pos = new UniformIntegerDistribution(0, this.vFlock.size()-1).sample();
			Bird currBird = this.vFlock.elementAt(pos);
			if(currBird.testVirus() && nSeed < this.vFlock.size()) i--;
			else currBird.setNDInfected(ndVirus);
		}
		this.populateComps();
	}
	
	public void forceND(int nSeed, NDInfection ndVirus){
		for(int i = 1; i <= nSeed; i++){
			int pos = 0;
			if(this.vFlock.size() > 1){
				pos = new UniformIntegerDistribution(0, this.vFlock.size()-1).sample();
				Bird currBird = this.vFlock.elementAt(pos);
				if(!currBird.testProtected()) currBird.setNDInfected(ndVirus);
			}
		}
		this.populateComps();
	}

//	Seed Newcastle disease in a flock given random introductions from a given virus
	public void seedNDSpark(NDInfection ndVirus, double transPara){
		int newInf = new BinomialDistribution(this.vFlock.size(), transPara).sample();
		if(newInf > 0){
		for(int i = 1; i <= newInf; i++){
			int pos = 0;
			if(this.vFlock.size() > 1) pos = new UniformIntegerDistribution(0, this.vFlock.size()-1).sample();
			Bird currBird = this.vFlock.elementAt(pos);
			if(!currBird.testProtected()) currBird.setNDInfected(ndVirus);
		}
		}
		this.populateComps();
	}
//	Seed Newcastle disease in a flock given random introductions from a virus pool	
	public void seedNDSpark(Vector<NDInfection> ndvPools, double transPara){
		for(int i = 0; i < ndvPools.size(); i++){
			int newInf = new BinomialDistribution(this.vFlock.size(), transPara).sample();
			if(newInf > 0){
				for(int k = 1; k <= newInf; k++){
					int pos = 0;
					if(this.vFlock.size() > 1) pos = new UniformIntegerDistribution(0, this.vFlock.size()-1).sample();
						Bird currBird = this.vFlock.elementAt(pos);
						if(!currBird.testProtected()) currBird.setNDInfected((NDInfection) ndvPools.elementAt(i));
					}
				}
			this.populateComps();
		}
	}
//	If there is between flock transmission then infect a random bird 
	public void ndBetweenFlock(NDInfection ndVirus){
		int pos = 0;
		if(this.vFlock.size() > 1) pos = new UniformIntegerDistribution(0, this.vFlock.size() - 1).sample();
		if(this.vFlock.size() > 0){
			Bird curr = this.vFlock.elementAt(pos);
			if(!curr.testProtected()) curr.setNDInfected(ndVirus);
			}
	}
//	Reset some counters
	private void initNDComps(){
		this.ndCompartments = new int[] {0,0,0,0,0};
		this.ndCompartmentsOut = new int[] {0,0,0,0,0};
	}
	public int[] getNDComps(){
		return this.ndCompartmentsOut;
	}
	public int[] getFlock(){
		int[] flock = {this.nChicksI, this.nGrowersI, this.nHensI, this.nCocks};
		return flock;
	}
//	Populate ND compartments	
	public void populateComps(){
		this.initNDComps();
		for(int i = 0; i < this.vFlock.size(); i++){
			Bird currBird = vFlock.elementAt(i);
//			if(!currBird.testVirus() && currBird.getAge() >= 42) this.ndCompartmentsOut[0]++;
			if(!currBird.testVirus()) this.ndCompartmentsOut[0]++;
			if(!currBird.testVirus()) this.ndCompartments[0]++;
			else{
				String cStatus = currBird.NDInf.getStatus();
				if(cStatus == "Latent") ndCompartments[1]++;
				if(cStatus == "Infectious") ndCompartments[2]++;
				if(cStatus == "Protected") ndCompartments[3]++;
				if(cStatus == "Recovered") ndCompartments[4]++;		

				if(cStatus == "Latent") this.ndCompartmentsOut[1]++;
				if(cStatus == "Infectious") this.ndCompartmentsOut[2]++;
				if(cStatus == "Protected") this.ndCompartmentsOut[3]++;
				if(cStatus == "Recovered") this.ndCompartmentsOut[4]++;		

			}
		}
	}
	
//	Second principal method that deals with ND in the flock in daily time steps	
	public void stepND(){
//	Counters		
		int countInfs = 0;
		this.ndDeaths = 0;
		this.ndDeathsChicks = 0;
		this.ndDeathsGrowers = 0;
		this.ndDeathsHens = 0;
		this.ndDeathsCocks = 0;
		this.deadNotVacc = 0;
		this.deadFail = 0;
		this.deadExpire = 0;
		this.ndViruses = new Vector<NDInfection>();
		this.infection = false;
//	Loop throuhg the flock
		for(int i = 0; i < this.vFlock.size(); i++){
			Bird currBird = vFlock.elementAt(i);
//	Increment the ND status of each bird
			if(currBird.testVaccinated() & currBird.testProtected()) currBird.incrementND();
//	If the bird has active infection then determine whether it dies			
			if(currBird.testVirus()){
				boolean dead = currBird.incrementND();
				if(dead) this.ndDeaths ++;	
				if(dead & currBird.getType() == "Chick") {
					this.ndDeathsChicks ++;
					this.nChicksI--;
				}
				if(dead & currBird.getType() == "Grower"){
					this.ndDeathsGrowers ++;
					this.nGrowersI--;
				}
				if(dead & currBird.getType() == "Hen") {
					this.ndDeathsHens ++;
					this.nHensI--;
				}
				if(dead & currBird.getType() == "Cock") {
					this.ndDeathsCocks ++;
					this.nCocks--;
				}
				if(dead & !currBird.getVaccBird()) this.deadNotVacc ++;
				if(dead & currBird.getVaccBird() & currBird.testVaccineFail()) this.deadFail ++;
				if(dead & currBird.getVaccBird() & !currBird.testVaccineFail()) this.deadExpire ++;
				if(currBird.NDInf.getStatus() == "Infectious"){
					this.infection = true;
					countInfs++;
				}
				if(dead){
					this.vFlock.removeElementAt(i);
					i--;
				}
			}
		}
//	Run through the flock again to deal with the infectious birds and determine transmission events
		for(int i = 0; i < this.vFlock.size(); i++){
			Bird currBird = vFlock.elementAt(i);
			if(currBird.testVirus()){ 
				if(currBird.NDInf.getStatus() == "Infectious" && currBird.NDInf.getType() != "Maternal" && currBird.NDInf.getType() != "Vaccine"){
					if(currBird.betweenFlock()) this.ndViruses.addElement(currBird.NDInf);
					int newInf = new BinomialDistribution(this.vFlock.size() - 1, currBird.NDInf.getTransmission() * this.seasonTransAdj * currBird.getInfTrans()).sample();
					for(int k = 1; k <= newInf; k++){
						int pos = new UniformIntegerDistribution(0, this.vFlock.size()-1).sample();
							if(pos != i){
								Bird nextBird = this.vFlock.elementAt(pos);
								if(!nextBird.testProtected()) nextBird.setNDInfected(currBird.NDInf);
					}
					else if(pos == i) k--;
				}
			}			
			}
		}
		this.populateComps();
	}

//	Deal with the vaccination status of the flock	
	public void vaccinate(NDInfection vaccine, int day){
		this.isVaccinated = true;
		this.vaccDay = day;
		for(int i = 0; i < this.vFlock.size(); i++){
			Bird curr  = this.vFlock.elementAt(i);
			curr.setVaccineFail();
			if(Math.random() < this.vEfficacy) curr.setNDVaccinated(vaccine);			
		}
	}
	
	public void setVaccination(int day, double efficacy){
		this.isVaccinated = true;
		this.vaccDay = day;
		this.vEfficacy = efficacy;
	}
	
	public boolean getVaccinated(){
		return this.isVaccinated;
	}
	
	public void adjOptSize(double adjustment){
		
		if(this.isVaccinated) this.optSize = this.optSize * adjustment;
	}
	public void improveProductivity(double imp){
		this.chickDeathA = this.chickDeathA / imp;
		this.hatchRate = this.hatchRate * imp;
	}
	public void adjustHenCockRatio(double ratio){
		this.henCockRatio = ratio;
	}
	public void adjustMaxChickAge(int cAge){
		this.maxChickAge = cAge;
	}
	public void adjustMaxGrowerAge(int cAge){
		this.maxGrowerAge = cAge;
	}

}
