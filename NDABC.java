import org.apache.commons.math3.distribution.*;
import java.util.Date;

public class NDABC {
	private String NDPath;
	private int latent;
	private int infectious;
	private int prot;
	private int titre;
	private int maternalA;
	private double mortality;
	private NDInfection hpndv;
	private double transmission;

	public static void main(String[] args) throws Exception{
// 		TODO Auto-generated method stub
		
//		Call and run the required method 
		NDABC myDriver = new NDABC();	
		//myDriver.second();
	//	myDriver.secondHaryana();
	//	myDriver.secondOrissa();
	//	myDriver.thirdOrissa();
	//	myDriver.firstEthiopia();
	//	myDriver.secondEthiopia();
	//	myDriver.fourthEthiopia();
	//	myDriver.firstBurkina();
	//	myDriver.secondBurkina();
	//	myDriver.thirdBurkina();
	//	myDriver.firstKenya();
	//	myDriver.secondKenya();
		myDriver.fifthEthiopia();
	}
	public NDABC(){
	
		this.NDPath = "High path";
		this.latent = 5;
		this.infectious = 5;
		this.prot = 105;
		this.titre = 300;
		this.maternalA = 42;
		this.mortality = 0.85/5;
		this.transmission = 3/(7.96*2.5);
		this.hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, this.transmission);
	}

		public void first() throws Exception {
			// Targets - Demography - MP
			double chicks = 4.21;
			double hens = 2.35;
			double cocks = 1.4;
			double dWeight = 1; 
			double eggOT = 11.7;
			double birdOT = 13.2;
			double otWeight = 0.5;
			double ndDeathsO = 8.34;
			double deathWeight = 0.5;
			
			// Parameters		
			double localTransLL = 0.15;
			double localTransUL = 0.3;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.015;
			double chickDeathUL = 0.04;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0005;
			double growerDeathUL = 0.001;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.005;
			double growerSalesUL = 0.02;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0005;
			double adultSalesUL = 0.0015;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + hens + cocks;
			double tConstant = 2.5;
			double transMean = 3.3;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 3.1;
			double hatchRate = 7.2;
			double eggOffTake = 1.6;
			double introConstant = 5;
			double localConstant = 2;
			double transConstant = 1.25;
						
			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.05;

			String file = "Outputs\\India\\ND\\ABC\\MP_ABC_8.txt";
			ReadWrite writer = new ReadWrite(file);

//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				double previousVal = 0;
				int currPara = paraSampler.sample();
				if(currPara < 6){
					previousVal = paraBucket[currPara];
					paraBucket[currPara] = distArray[currPara].sample();
				}
				if(currPara == 6){
					previousVal = transC;
					transC = transD.sample() / (flockSize * tConstant);					
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsMP = {chicks, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.001};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 110, paramsMP);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				Flock[] trojan = trojans(paramsMP, 5);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh
					if(i == 365 * 2) test.setNDVs(0.00001, paraBucket[5] / localConstant, transC / transConstant);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, paraBucket[5], transC);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, paraBucket[5] / localConstant, transC / transConstant);				
					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(5);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / 5);
						tChicks = tChicks + ((anni.nChicksI + anni.nGrowersI) / 5);
						tHens = tHens + (anni.nHensI / 5);
						tCocks = tCocks + (anni.nCocks / 5);
						ndDeaths = ndDeaths + (anni.getndDeathsAge()[0] / 5);
						}
					}
				}
				double chickDiff = ((tChicks / (365*nYear)) - chicks) * dWeight;
				double henDiff = ((tHens / (365*nYear)) - hens) * dWeight;
				double cockDiff = ((tCocks / (365*nYear)) - cocks) * dWeight;
				double salesDiff = ((tSales / nYear) - birdOT) * otWeight;
				double ndDiff = ((ndDeaths / nYear) - ndDeathsO) * deathWeight;
				
				double est = this.mod(chickDiff) + this.mod(henDiff) + this.mod(cockDiff) + this.mod(salesDiff) + this.mod(ndDiff);
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < currVal * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}

		public void second() throws Exception {
			// Targets - Demography - MP
			double chicks = 4.21;
			double hens = 2.35;
			double cocks = 1.4;
			double dWeight = 1;
			double hWeight = 1.5;
			double eggOT = 11.7;
			double birdOT = 13.2;
			double otWeight = 0.3;
			double ndDeathsO = 8.34;
			double deathWeight = 0.4;
			
			// Parameters		
			double localTransLL = 0.15;
			double localTransUL = 0.3;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.015;
			double chickDeathUL = 0.04;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0005;
			double growerDeathUL = 0.001;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.005;
			double growerSalesUL = 0.02;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0005;
			double adultSalesUL = 0.0015;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + hens + cocks;
			double tConstant = 2.5;
			double transMean = 3.3;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 3.1;
			double hatchRate = 7.2;
			double eggOffTake = 1.6;
			double introConstant = 5;
			double localConstant = 2;
			double transConstant = 1.25;
						
			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.05 * 0.675, 0.0015 * 0.5, 0.0007 * 0.5, 0.019 * 0.7, 0.0009, 0.24};
			transC = 3.3/(7.96*2.5);
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.05;

			String file = "Outputs\\India\\ND\\ABC\\MP_ABC_s4.txt";
			ReadWrite writer = new ReadWrite(file);

//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsMP = {chicks, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.001};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 110, paramsMP);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 20;
				Flock[] trojan = trojans(paramsMP, nTrojans);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh
					if(i == 365 * 2) test.setNDVs(0.00001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, paraBucket[5] / localConstant, 1 / transConstant);				
					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double)(anni.nChicksI + anni.nGrowersI) / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						}
					}
				}
				double chickDiff = ((tChicks / (365*nYear)) - chicks) * dWeight;
				double henDiff = ((tHens / (365*nYear)) - hens) * hWeight;
				double cockDiff = ((tCocks / (365*nYear)) - cocks) * dWeight;
				double salesDiff = ((tSales / nYear) - birdOT) * otWeight;
				double ndDiff = ((ndDeaths / nYear) - ndDeathsO) * deathWeight;
				
				double est = this.mod(chickDiff) + this.mod(henDiff) + this.mod(cockDiff) + this.mod(salesDiff) + this.mod(ndDiff);
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < currVal * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}

		public void secondHaryana() throws Exception {
			// Targets - Demography - MP
			double chicks = 1.83;
			double hens = 1.6;
			double cocks = 1.02;
			double dWeight = 0.8;
			double hWeight = 1.5;
			double eggOT = 23.1;
			double birdOT = 11.4;
			double otWeight = 0.3;
			double ndDeathsO = 6.84;
			double deathWeight = 0.4;
			
			// Parameters		
			double localTransLL = 0.2;
			double localTransUL = 0.35;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.02;
			double chickDeathUL = 0.045;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0005;
			double growerDeathUL = 0.001;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.005;
			double growerSalesUL = 0.02;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0005;
			double adultSalesUL = 0.0015;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + hens + cocks;
			double tConstant = 2.5;
			double transMean = 3.3;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 3.8;
			double hatchRate = 6.6;
			double eggOffTake = 3.8;
			double introConstantA = 10;
			double localConstantA = 3;
			double transConstantA = 1.35;

			double introConstantB = 10;
			double localConstantB = 2;
			double transConstantB = 1.15;
						
			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.04*0.9, 0.001 * 0.7, 0.0005 * 0.7, 0.017 * 0.8, 0.00085 * 0.8, 0.3};
			transC = 3/(4.45*2.5);
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.05;

			String file = "Outputs\\India\\ND\\ABC\\Haryana\\Haryana_ABC_s3.txt";
			ReadWrite writer = new ReadWrite(file);

//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsH = {chicks, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.001};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 312, paramsH);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 20;
				Flock[] trojan = trojans(paramsH, nTrojans);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh
					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, paraBucket[5] / localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, paraBucket[5] / localConstantB, 1 / transConstantB);
					else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, paraBucket[5] / localConstantA, 1 / transConstantA);

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double)(anni.nChicksI + anni.nGrowersI) / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						}
					}
				}
				double chickDiff = ((tChicks / (365*nYear)) - chicks) * dWeight;
				double henDiff = ((tHens / (365*nYear)) - hens) * hWeight;
				double cockDiff = ((tCocks / (365*nYear)) - cocks) * dWeight;
				double salesDiff = ((tSales / nYear) - birdOT) * otWeight;
				double ndDiff = ((ndDeaths / nYear) - ndDeathsO) * deathWeight;
				
				double est = this.mod(chickDiff) + this.mod(henDiff) + this.mod(cockDiff) + this.mod(salesDiff) + this.mod(ndDiff);
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < currVal * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}
		public void secondOrissa() throws Exception {
			// Targets - Demography - MP
			double chicks = 3.8;
			double hens = 2.03;
			double cocks = 1.31;
			double dWeight = 1.2;
			double hWeight = 1.5;
			double eggOT = 17.1;
			double birdOT = 15.9;
			double otWeight = 0.3;
			double ndDeathsO = 19.74;
			double deathWeight = 0.3;
			
			// Parameters		
			double localTransLL = 0.2;
			double localTransUL = 0.35;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.005;
			double chickDeathUL = 0.025;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0005;
			double growerDeathUL = 0.001;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.005;
			double growerSalesUL = 0.02;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0005;
			double adultSalesUL = 0.0015;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + hens + cocks;
			double tConstant = 2.5;
			double transMean = 3.3;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.9;
			double hatchRate = 5.9;
			double eggOffTake = 2.9;
			double introConstant = 10;
			double localConstant = 2.6;
			double transConstant = 1.35;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.03 * 0.6, 0.0015 * 0.6, 0.0005 * 0.6, 0.019 * 0.65, 0.00085 * 0.65, 0.26};
			transC = 3.3/(7.15*2.5);
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.05;

			String file = "Outputs\\India\\ND\\ABC\\Orissa\\Orisse_ABC_s7.txt";
			ReadWrite writer = new ReadWrite(file);

//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsO = {chicks, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.001};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 332, paramsO);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 20;
				Flock[] trojan = trojans(paramsO, nTrojans);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double)(anni.nChicksI + anni.nGrowersI) / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						}
					}
				}
				double chickDiff = ((tChicks / (365*nYear)) - chicks) * dWeight;
				double henDiff = ((tHens / (365*nYear)) - hens) * hWeight;
				double cockDiff = ((tCocks / (365*nYear)) - cocks) * dWeight;
				double salesDiff = ((tSales / nYear) - birdOT) * otWeight;
				double ndDiff = ((ndDeaths / nYear) - ndDeathsO) * deathWeight;
				
				double est = this.mod(chickDiff) + this.mod(henDiff) + this.mod(cockDiff) + this.mod(salesDiff) + this.mod(ndDiff);
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < currVal * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}
		public void thirdOrissa() throws Exception {
			// Targets - Demography - MP
			double chicks = 3.8;
			double hens = 2.03;
			double cocks = 1.31;
			double dWeight = 1.5;
			double hWeight = 1.5;
			double cWeight = 1;
			double eggOT = 17.1;
			double birdOT = 15.9;
			double otWeight = 0.3;
			double ndDeathsO = 19.74;
			double deathWeight = 0.3;
			
			// Parameters		
			double localTransLL = 0.25;
			double localTransUL = 0.4;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.005;
			double chickDeathUL = 0.02;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0005;
			double growerDeathUL = 0.001;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.005;
			double growerSalesUL = 0.02;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0001;
			double adultSalesUL = 0.0008;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + hens + cocks;
			double tConstant = 2.5;
			double transMean = 3.94;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.9;
			double hatchRate = 5.9;
			double eggOffTake = 2.9;
			double introConstant = 10;
			double localConstant = 2.6;
			double transConstant = 1.35;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.0092, 0.000685, 0.000229, 0.0128, 0.000569, 0.355};
			transC = 3.94/(7.15*2.5);
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.05;

			String file = "Outputs\\India\\ND\\ABC\\Orissa\\Orisse_ABC_t7.txt";
			ReadWrite writer = new ReadWrite(file);

//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsO = {chicks, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.001};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 332, paramsO);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 20;
				Flock[] trojan = trojans(paramsO, nTrojans);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double)(anni.nChicksI + anni.nGrowersI) / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						}
					}
				}
				double chickDiff = ((tChicks / (365*nYear)) - chicks) * dWeight;
				double henDiff = ((tHens / (365*nYear)) - hens) * hWeight;
				double cockDiff = ((tCocks / (365*nYear)) - cocks) * cWeight;
				double salesDiff = ((tSales / nYear) - birdOT) * otWeight;
				double ndDiff = ((ndDeaths / nYear) - ndDeathsO) * deathWeight;
				
				double est = this.mod(chickDiff) + this.mod(henDiff) + this.mod(cockDiff) + this.mod(salesDiff) + this.mod(ndDiff);
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < currVal * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}
		public void firstEthiopia() throws Exception {

			double chicks = 2.84;
			double growers = 1.73;
			double hens = 2.44;
			double cocks = 0.61;
			double dWeight = 1.5;
			double hWeight = 1.5;
			double cWeight = 1;
			double birdOT = 8.45;
			double otWeight = 0.3;
			double ndDeathsO = 0.542;
			double deathWeight = 20;
			
			// Parameters		
			double localTransLL = 0.1;
			double localTransUL = 0.4;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.005;
			double chickDeathUL = 0.04;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0005;
			double growerDeathUL = 0.001;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.001;
			double growerSalesUL = 0.02;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0001;
			double adultSalesUL = 0.0008;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + hens + cocks;
			double tConstant = 2.5;
			double transMean = 3.94;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.5;
			double hatchRate = 6.7;
			double eggOffTake = 4.5;
			double introConstant = 10;
			double localConstant = 2.;
			double transConstant = 1.35;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.04, 0.00137, 0.00034, 0.018, 0.0005, 0.355};
			transC = 3./(7.6*2.5);
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.05;

			String file = "Outputs\\Africa\\Ethiopia\\Ethiopia_ABC_f1.txt";
			ReadWrite writer = new ReadWrite(file);

//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsE = {chicks, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.001};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 250, paramsE);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 20;
				Flock[] trojan = trojans(paramsE, nTrojans);
				test.forceTrojan(trojan);
				test.setHenCockRatio(4);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tGrowers = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				double nonNDDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 242) % 365 == 0) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double) anni.nChicksI / nTrojans);
						tGrowers = tGrowers + ((double) anni.nGrowersI / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						nonNDDeaths = nonNDDeaths + ((double) anni.getTotalDeaths() / nTrojans);
						}
					}
				}
				double chickDiff = ((tChicks / (365*nYear)) - chicks) * dWeight;
				double growerDiff = ((tGrowers / (365*nYear)) - growers) * dWeight;
				double henDiff = ((tHens / (365*nYear)) - hens) * hWeight;
				double cockDiff = ((tCocks / (365*nYear)) - cocks) * cWeight;
				double salesDiff = ((tSales / nYear) - birdOT) * otWeight;
				double ndDiff = (ndDeathsO - (ndDeaths / (ndDeaths + nonNDDeaths))) * deathWeight;
				
				double est = this.mod(chickDiff) + this.mod(growerDiff) + this.mod(henDiff) + this.mod(cockDiff) + this.mod(salesDiff) + this.mod(ndDiff);
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < currVal * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " " + tGrowers/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10+ " "+ nonNDDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}
		public void secondEthiopia() throws Exception {

			double chicks = 2.84;
			double growers = 1.73;
			double hens = 2.44;
			double cocks = 0.61;
			double dWeight = 1.;
			double hWeight = 1.5;
			double cWeight = 1;
			double birdOT = 8.45;
			double otWeight = 0.5;
			double ndDeathsO = 0.542;
			double deathWeight = 20;
			
			// Parameters		
			double localTransLL = 0.1;
			double localTransUL = 0.4;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.005;
			double chickDeathUL = 0.04;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0005;
			double growerDeathUL = 0.0012;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.001;
			double growerSalesUL = 0.02;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0001;
			double adultSalesUL = 0.0008;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + hens + cocks;
			double tConstant = 2.5;
			double transMean = 3.94;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.5;
			double hatchRate = 6.7;
			double eggOffTake = 4.5;
			double introConstant = 10;
			double localConstant = 2.;
			double transConstant = 1.35;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.0135, 0.000977, 0.000302, 0.01088, 0.000618, 0.362};
			transC = 0.233;
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.02;

			String file = "Outputs\\Africa\\Ethiopia\\Ethiopia_ABC_sP10.txt";
			ReadWrite writer = new ReadWrite(file);

//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsE = {chicks, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.001};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 250, paramsE);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 20;
				Flock[] trojan = trojans(paramsE, nTrojans);
				test.forceTrojan(trojan);
				test.setHenCockRatio(4);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tGrowers = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				double nonNDDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 242) % 365 == 0) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double) anni.nChicksI / nTrojans);
						tGrowers = tGrowers + ((double) anni.nGrowersI / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						nonNDDeaths = nonNDDeaths + ((double) anni.getTotalDeaths() / nTrojans);
						}
					}
				}
				double chickDiff = ((tChicks / (365*nYear)) - chicks) * dWeight;
				double growerDiff = ((tGrowers / (365*nYear)) - growers) * dWeight;
				double henDiff = ((tHens / (365*nYear)) - hens) * hWeight;
				double cockDiff = ((tCocks / (365*nYear)) - cocks) * cWeight;
				double salesDiff = ((tSales / nYear) - birdOT) * otWeight;
				double ndDiff = (ndDeathsO - (ndDeaths / (ndDeaths + nonNDDeaths))) * deathWeight;
				
				double est = this.mod(chickDiff) + this.mod(growerDiff) + this.mod(henDiff) + this.mod(cockDiff) + this.mod(salesDiff) + this.mod(ndDiff);
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < currVal * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " " + tGrowers/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10+ " "+ nonNDDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}
		public void thirdEthiopia() throws Exception {

			double chicks = 2.84;
			double growers = 1.73;
			double hens = 2.44;
			double cocks = 0.61;
			double dWeight = 1.;
			double hWeight = 1.5;
			double cWeight = 1;
			double birdOT = 8.45;
			double otWeight = 1.5;
			double ndDeathsO = 0.542;
			double deathWeight = 20;
			
			// Parameters		
			double localTransLL = 0.3;
			double localTransUL = 0.4;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.01;
			double chickDeathUL = 0.02;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0007;
			double growerDeathUL = 0.0012;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.005;
			double growerSalesUL = 0.015;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0001;
			double adultSalesUL = 0.0008;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + growers + hens + cocks;
			double tConstant = 2.5;
			double transMean = 4.66;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.5;
			double hatchRate = 6.7;
			double eggOffTake = 4.5;
			double introConstant = 10;
			double localConstant = 2.;
			double transConstant = 1.35;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.0174, 0.00105, 0.000308, 0.01262, 0.000638, 0.36};
			transC = 0.24478;
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.1;

			String file = "Outputs\\Africa\\Ethiopia\\Ethiopia_ABC_t11.txt";
			ReadWrite writer = new ReadWrite(file);

//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsE = {chicks + growers, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.001};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 220, paramsE);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 40;
				Flock[] trojan = trojans(paramsE, nTrojans);
				test.forceTrojan(trojan);
				test.setHenCockRatio(4);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tGrowers = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				double nonNDDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 242) % 365 == 0) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double) anni.nChicksI / nTrojans);
						tGrowers = tGrowers + ((double) anni.nGrowersI / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						nonNDDeaths = nonNDDeaths + ((double) anni.getTotalDeaths() / nTrojans);
						}
					}
				}
				double chickDiff = ((tChicks / (365*nYear)) - chicks) * dWeight;
				double growerDiff = ((tGrowers / (365*nYear)) - growers) * dWeight;
				double henDiff = ((tHens / (365*nYear)) - hens) * hWeight;
				double cockDiff = ((tCocks / (365*nYear)) - cocks) * cWeight;
				double salesDiff = ((tSales / nYear) - birdOT) * otWeight;
				double ndDiff = (ndDeathsO - (ndDeaths / (ndDeaths + nonNDDeaths))) * deathWeight;
				
				double est = this.mod(chickDiff) + this.mod(growerDiff) + this.mod(henDiff) + this.mod(cockDiff) + this.mod(salesDiff) + this.mod(ndDiff);
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < currVal * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " " + tGrowers/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10+ " "+ nonNDDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}
		public void fourthEthiopia() throws Exception {

			double chicks = 2.84;
			double growers = 1.73;
			double hens = 2.44;
			double cocks = 0.61;
			double dWeight = 1.;
			double hWeight = 1.5;
			double cWeight = 1;
			double birdOT = 8.45;
			double otWeight = 1.5;
			double ndDeathsO = 0.542;
			double deathWeight = 20;
			
			// Parameters		
			double localTransLL = 0.3;
			double localTransUL = 0.45;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.01;
			double chickDeathUL = 0.025;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0007;
			double growerDeathUL = 0.0012;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.005;
			double growerSalesUL = 0.018;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0001;
			double adultSalesUL = 0.0012;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + growers + hens + cocks;
			double tConstant = 2.5;
			double transMean = 5.56;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.5;
			double hatchRate = 6.7;
			double eggOffTake = 4.5;
			double introConstant = 10;
			double localConstant = 2.;
			double transConstant = 1.35;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.0191, 0.000975, 0.000358, 0.01477, 0.000876, 0.375};
			transC = 0.2927;
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.1;

			String file = "Outputs\\Africa\\Ethiopia\\Ethiopia_ABC_f5.txt";
			ReadWrite writer = new ReadWrite(file);
//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsE = {chicks + growers, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.001};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 300, paramsE);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 40;
				Flock[] trojan = trojans(paramsE, nTrojans);
				test.forceTrojan(trojan);
				test.setHenCockRatio(4);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tGrowers = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				double nonNDDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 242) % 365 == 0) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double) anni.nChicksI / nTrojans);
						tGrowers = tGrowers + ((double) anni.nGrowersI / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						nonNDDeaths = nonNDDeaths + ((double) anni.getTotalDeaths() / nTrojans);
						}
					}
				}
				double chickDiff = ((tChicks / (365*nYear)) - chicks) * dWeight;
				double growerDiff = ((tGrowers / (365*nYear)) - growers) * dWeight;
				double henDiff = ((tHens / (365*nYear)) - hens) * hWeight;
				double cockDiff = ((tCocks / (365*nYear)) - cocks) * cWeight;
				double salesDiff = ((tSales / nYear) - birdOT) * otWeight;
				double ndDiff = (ndDeathsO - (ndDeaths / (ndDeaths + nonNDDeaths))) * deathWeight;
				
				double est = this.mod(chickDiff) + this.mod(growerDiff) + this.mod(henDiff) + this.mod(cockDiff) + this.mod(salesDiff) + this.mod(ndDiff);
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < currVal * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " " + tGrowers/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10+ " "+ nonNDDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}
		public void fifthEthiopia() throws Exception {

			double chicks = 2.84;
			double growers = 1.73;
			double hens = 2.44;
			double cocks = 0.61;
			double dWeight = 1.;
			double hWeight = 1.5;
			double cWeight = 1;
			double birdOT = 8.45;
			double otWeight = 1.;
			double seroND = 0.222;
			double ndDeathsO = 0.542;
			double deathWeight = 1;
			
			// Parameters		
			double localTransLL = 0.15;
			double localTransUL = 0.45;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.015;
			double chickDeathUL = 0.035;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0007;
			double growerDeathUL = 0.002;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0007;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.005;
			double growerSalesUL = 0.02;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0001;
			double adultSalesUL = 0.0012;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + growers + hens + cocks;
			double tConstant = 2.5;
			double transMean = 4.59;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.5;
			double hatchRate = 6.7;
			double eggOffTake = 4.5;
			double introConstant = 10;
			double localConstant = 2.;
			double transConstant = 1.35;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			double paraBucket[] = {0.0336, 0.00191, 0.0003454, 0.0149, 0.001186, 0.4073};
			transC = 0.2417;
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.1;

			String file = "Outputs\\Africa\\Ethiopia\\Ethiopia_ABC_fifth2.txt";
			ReadWrite writer = new ReadWrite(file);
//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsE = {chicks + growers, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.001};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 300, paramsE);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 40;
				Flock[] trojan = trojans(paramsE, nTrojans);
				test.forceTrojan(trojan);
				test.setHenCockRatio(4);
				test.seedND(hpndv);
				test.setNDVs(0.00005, paraBucket[5], 1);
				double tSales = 0;
				double tChicks = 0;
				double tGrowers = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				double nonNDDeaths = 0;
				double ndProtected = 0;
				boolean flag = false;
				int nYear = 15;
				for(int i = 0; i < 365 * (5 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000005, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00005, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 242) % 365 == 0) test.setNDVs(0.000005, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 5){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double) anni.nChicksI / nTrojans);
						tGrowers = tGrowers + ((double) anni.nGrowersI / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						nonNDDeaths = nonNDDeaths + ((double) anni.getTotalDeaths() / nTrojans);
						ndProtected = ndProtected + (((double) anni.getNDComps()[3] + anni.getNDComps()[4])/nTrojans);
						}
					}
				}
				double chickDiff = (this.mod(((tChicks / (365*nYear)) - chicks)) / chicks) * dWeight;
				double growerDiff = (this.mod(((tGrowers / (365*nYear)) - growers)) / growers) * dWeight;
				double henDiff = (this.mod(((tHens / (365*nYear)) - hens)) / hens) * hWeight;
				double cockDiff = (this.mod(((tCocks / (365*nYear)) - cocks)) / cocks) * cWeight;
				double salesDiff = (this.mod(((tSales / nYear) - birdOT)) / birdOT) * otWeight;
				double ndDiff = (ndDeathsO - (ndDeaths / (ndDeaths + nonNDDeaths))) * deathWeight;
				double seroPrev = ndProtected / (tGrowers + tHens + tCocks);
				double ndSero = (this.mod(seroND - (seroPrev))/seroND) * deathWeight;
				
				double est = chickDiff + growerDiff + henDiff + cockDiff + salesDiff + ndSero;
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < minEst * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/nYear + " " + tChicks/(365*nYear) + " " + tGrowers/(365*nYear) + " "+ tHens/(365*nYear) + " " + tCocks/(365*nYear) + " "+ ndDeaths/nYear+ " "+ nonNDDeaths/nYear + " "+seroPrev);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}

		public void firstBurkina() throws Exception {

			double chicks = 20;
			double growers = 6.4;
			double hens = 5.5;
			double cocks = 1.6;
			double dWeight = 1.;
			double hWeight = 2;
			double cWeight = 0.8;
			double birdOT = 42.36;
			double otWeight = 0.8;
			double ndDeathsO = 0.7;
			double deathWeight = 20;
			double deaths = 49.6;
			double deathWeightA = 1;
			
			// Parameters		
			double localTransLL = 0.1;
			double localTransUL = 0.4;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.01;
			double chickDeathUL = 0.03;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0007;
			double growerDeathUL = 0.0015;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.01;
			double growerSalesUL = 0.03;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0005;
			double adultSalesUL = 0.0015;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + growers + hens + cocks;
			double tConstant = 2.5;
			double transMean = 2.31;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.2;
			double hatchRate = 7.6;
			double eggOffTake = 4.2;
			double introConstant = 10;
			double localConstant = 2.;
			double transConstant = 1.35;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.01, 0.000926, 0.000345, 0.027, 0.001, 0.1886};
			transC = 2.31 / (33 * 2.5);
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.1;

			String file = "Outputs\\Africa\\BurkinaFaso\\Burkina_ABC_f3.txt";
			ReadWrite writer = new ReadWrite(file);
//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsB = {chicks + growers, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.003};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 200, paramsB);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 40;
				Flock[] trojan = trojans(paramsB, nTrojans);
				test.forceTrojan(trojan);
				test.setHenCockRatio(3.4);
				test.setMaxChickAge(60);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tGrowers = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				double nonNDDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 270) % 365 == 0) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double) anni.nChicksI / nTrojans);
						tGrowers = tGrowers + ((double) anni.nGrowersI / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						nonNDDeaths = nonNDDeaths + ((double) anni.getTotalDeaths() / nTrojans);
						}
					}
				}
				double chickDiff = ((tChicks / (365*nYear)) - chicks) * dWeight;
				double growerDiff = ((tGrowers / (365*nYear)) - growers) * dWeight;
				double henDiff = ((tHens / (365*nYear)) - hens) * hWeight;
				double cockDiff = ((tCocks / (365*nYear)) - cocks) * cWeight;
				double salesDiff = ((tSales / nYear) - birdOT) * otWeight;
				double ndDiff = (ndDeathsO - (ndDeaths / (ndDeaths + nonNDDeaths))) * deathWeight;
				double deathDiff = ((deaths - (ndDeaths + nonNDDeaths)) / nYear) * deathWeightA;
			
				double est = this.mod(chickDiff) + this.mod(growerDiff) + this.mod(henDiff) + this.mod(cockDiff) + this.mod(salesDiff) + this.mod(ndDiff) + this.mod(deathDiff);
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < minEst * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " " + tGrowers/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10+ " "+ nonNDDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}
		public void secondBurkina() throws Exception {

			double chicks = 20;
			double growers = 6.4;
			double hens = 5.5;
			double cocks = 1.6;
			double dWeight = 1.5;
			double hWeight = 1;
			double cWeight = 1;
			double birdOT = 42.36;
			double otWeight = 1;
			double ndDeathsO = 0.7;
			double deathWeight = 1;
			double deaths = 73.8;
			double deathWeightA = 1;
			
			// Parameters		
			double localTransLL = 0.1;
			double localTransUL = 0.4;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.005;
			double chickDeathUL = 0.02;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0007;
			double growerDeathUL = 0.0015;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.01;
			double growerSalesUL = 0.03;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0005;
			double adultSalesUL = 0.0015;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + growers + hens + cocks;
			double tConstant = 2.5;
			double transMean = 2.31;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.2;
			double hatchRate = 7.6;
			double eggOffTake = 4.2;
			double introConstant = 10;
			double localConstant = 2.;
			double transConstant = 1.35;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.006, 0.000926, 0.000345, 0.022, 0.0012, 0.295};
			transC = 3 / (33 * 2.5);
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.1;

			String file = "Outputs\\Africa\\BurkinaFaso\\Burkina_ABC_s2.txt";
			ReadWrite writer = new ReadWrite(file);
//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsB = {chicks + growers, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.003};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 200, paramsB);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 40;
				Flock[] trojan = trojans(paramsB, nTrojans);
				test.forceTrojan(trojan);
				test.setHenCockRatio(3.4);
				test.setMaxChickAge(60);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tGrowers = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				double nonNDDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 270) % 365 == 0) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double) anni.nChicksI / nTrojans);
						tGrowers = tGrowers + ((double) anni.nGrowersI / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						nonNDDeaths = nonNDDeaths + ((double) anni.getTotalDeaths() / nTrojans);
						}
					}
				}
				double chickDiff = (this.mod(((tChicks / (365*nYear)) - chicks)) / chicks) * dWeight;
				double growerDiff = (this.mod(((tGrowers / (365*nYear)) - growers)) / growers) * dWeight;
				double henDiff = (this.mod(((tHens / (365*nYear)) - hens)) / hens) * hWeight;
				double cockDiff = (this.mod(((tCocks / (365*nYear)) - cocks)) / cocks) * cWeight;
				double salesDiff = (this.mod(((tSales / nYear) - birdOT)) / birdOT) * otWeight;
				double ndDiff = (ndDeathsO - (ndDeaths / (ndDeaths + nonNDDeaths))) * deathWeight;
				double deathDiff = (this.mod(((deaths - (ndDeaths + nonNDDeaths)) / nYear)) / deaths) * deathWeightA;
			
				double est = chickDiff + growerDiff + henDiff + cockDiff + salesDiff + ndDiff + deathDiff;
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < minEst * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " " + tGrowers/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10+ " "+ nonNDDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}
		public void thirdBurkina() throws Exception {

			double chicks = 20;
			double growers = 6.4;
			double hens = 6.2;
			double cocks = 1.8;
			double dWeight = 1.;
			double hWeight = 1;
			double cWeight = 0.5;
			double birdOT = 42.36;
			double otWeight = 1;
			double ndDeathsO = 0.7;
			double deathWeight = 0.5;
			double deaths = 73.8;
			double deathWeightA = 0.5;
			
			// Parameters		
			double localTransLL = 0.1;
			double localTransUL = 0.4;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.005;
			double chickDeathUL = 0.02;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0007;
			double growerDeathUL = 0.0015;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.01;
			double growerSalesUL = 0.03;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0005;
			double adultSalesUL = 0.0015;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + growers + hens + cocks;
			double tConstant = 2.5;
			double transMean = 2.31;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.5;
			double hatchRate = 7.6;
			double eggOffTake = 4.2;
			double introConstant = 10;
			double localConstant = 2.6;
			double transConstant = 2.6;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.0061, 0.00124, 0.000444, 0.0207, 0.0013, 0.3476};
//			transC = 3 / (33 * 2.5);
			transC = 2.75 / (33 * 2.5);
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.05;

			String file = "Outputs\\Africa\\BurkinaFaso\\Burkina_ABC_thirdRev1.txt";
			ReadWrite writer = new ReadWrite(file);
//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsB = {chicks + growers, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.003};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 200, paramsB);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 40;
				Flock[] trojan = trojans(paramsB, nTrojans);
				test.forceTrojan(trojan);
				test.setHenCockRatio(3.4);
				test.setMaxChickAge(56);
				test.seedND(hpndv);
				test.setNDVs(0.00005, paraBucket[5], 1);
				double tSales = 0;
				double tChicks = 0;
				double tGrowers = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				double nonNDDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 270) % 365 == 0) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double) anni.nChicksI / nTrojans);
						tGrowers = tGrowers + ((double) anni.nGrowersI / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						nonNDDeaths = nonNDDeaths + ((double) anni.getTotalDeaths() / nTrojans);
						}
					}
				}
				double chickDiff = (this.mod(((tChicks / (365*nYear)) - chicks)) / chicks) * dWeight;
				double growerDiff = (this.mod(((tGrowers / (365*nYear)) - growers)) / growers) * dWeight;
				double henDiff = (this.mod(((tHens / (365*nYear)) - hens)) / hens) * hWeight;
				double cockDiff = (this.mod(((tCocks / (365*nYear)) - cocks)) / cocks) * cWeight;
				double salesDiff = (this.mod(((tSales / nYear) - birdOT)) / birdOT) * otWeight;
				double ndDiff = this.mod(ndDeathsO - (ndDeaths / (ndDeaths + nonNDDeaths))) * deathWeight;
				double deathDiff = (this.mod(deaths - ((ndDeaths + nonNDDeaths) / nYear)) / deaths) * deathWeightA;
			
				double est = chickDiff + growerDiff + henDiff + cockDiff + salesDiff + ndDiff + deathDiff;
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < minEst * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " " + tGrowers/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10+ " "+ nonNDDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}
		public void firstKenya() throws Exception {

			double chicks = 7.75;
			double growers = 5.2;
			double hens = 4.45;
			double cocks = 1.51;
			double dWeight = 1.;
			double hWeight = 1;
			double cWeight = 0.5;
			double birdOT = 26.1;
			double otWeight = 1;
			double ndDeathsO = 0.4;
			double deathWeight = 0.5;
			double deaths = 60.78;
			double deathWeightA = 0.5;
			
			// Parameters		
			double localTransLL = 0.1;
			double localTransUL = 0.4;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.005;
			double chickDeathUL = 0.02;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0007;
			double growerDeathUL = 0.0015;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.01;
			double growerSalesUL = 0.03;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0005;
			double adultSalesUL = 0.0015;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + growers + hens + cocks;
			double tConstant = 2.5;
			double transMean = 2.31;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.5;
			double hatchRate = 7.8;
			double eggOffTake = 2.86;
			double introConstant = 10;
			double localConstant = 2.;
			double transConstant = 1.35;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.006, 0.000926, 0.0003, 0.012, 0.0006, 0.295};
			transC = 3 / (18.91 * 2.5);
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.05;

			String file = "Outputs\\Africa\\Tanzania\\Tanzania_ABC_t1.txt";
			ReadWrite writer = new ReadWrite(file);
//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsB = {chicks + growers, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.003};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 200, paramsB);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 40;
				Flock[] trojan = trojans(paramsB, nTrojans);
				test.forceTrojan(trojan);
				test.setHenCockRatio(3.);
				test.setMaxChickAge(56);
				test.setMaxGrowerAge(180);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tGrowers = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				double nonNDDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 270) % 365 == 0) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double) anni.nChicksI / nTrojans);
						tGrowers = tGrowers + ((double) anni.nGrowersI / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						nonNDDeaths = nonNDDeaths + ((double) anni.getTotalDeaths() / nTrojans);
						}
					}
				}
				double chickDiff = (this.mod(((tChicks / (365*nYear)) - chicks)) / chicks) * dWeight;
				double growerDiff = (this.mod(((tGrowers / (365*nYear)) - growers)) / growers) * dWeight;
				double henDiff = (this.mod(((tHens / (365*nYear)) - hens)) / hens) * hWeight;
				double cockDiff = (this.mod(((tCocks / (365*nYear)) - cocks)) / cocks) * cWeight;
				double salesDiff = (this.mod(((tSales / nYear) - birdOT)) / birdOT) * otWeight;
				double ndDiff = this.mod(ndDeathsO - (ndDeaths / (ndDeaths + nonNDDeaths))) * deathWeight;
				double deathDiff = (this.mod(deaths - ((ndDeaths + nonNDDeaths) / nYear)) / deaths) * deathWeightA;
			
				double est = chickDiff + growerDiff + henDiff + cockDiff + salesDiff + ndDiff + deathDiff;
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < minEst * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " " + tGrowers/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10+ " "+ nonNDDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}
		public void secondKenya() throws Exception {

			double chicks = 7.75;
			double growers = 5.2;
			double hens = 4.45;
			double cocks = 1.51;
			double dWeight = 1.;
			double hWeight = 1;
			double cWeight = 0.5;
			double birdOT = 26.1;
			double otWeight = 1;
			double ndDeathsO = 0.4;
			double deathWeight = 0.5;
			double deaths = 60.78;
			double deathWeightA = 0.5;
			
			// Parameters		
			double localTransLL = 0.1;
			double localTransUL = 0.4;
			UniformRealDistribution localTransD = new UniformRealDistribution(localTransLL, localTransUL);
			
			double chickDeathLL = 0.005;
			double chickDeathUL = 0.02;
			UniformRealDistribution chickDeathD = new UniformRealDistribution(chickDeathLL, chickDeathUL);
			
			double growerDeathLL = 0.0007;
			double growerDeathUL = 0.0015;
			UniformRealDistribution growerDeathD = new UniformRealDistribution(growerDeathLL, growerDeathUL);
		
			double adultDeathLL = 0.0002;
			double adultDeathUL = 0.0005;
			UniformRealDistribution adultDeathD = new UniformRealDistribution(adultDeathLL, adultDeathUL);
			
			double growerSalesLL = 0.01;
			double growerSalesUL = 0.03;
			UniformRealDistribution growerSalesD = new UniformRealDistribution(growerSalesLL, growerSalesUL);
			
			double adultSalesLL = 0.0005;
			double adultSalesUL = 0.0015;
			UniformRealDistribution adultSalesD = new UniformRealDistribution(adultSalesLL, adultSalesUL);
			
			UniformRealDistribution[] distArray = {chickDeathD, growerDeathD, adultDeathD, growerSalesD, adultSalesD, localTransD};

			double flockSize = chicks + growers + hens + cocks;
			double tConstant = 2.5;
			double transMean = 2.31;
			double transsd = 0.5;
			NormalDistribution transD = new NormalDistribution(transMean, transsd);

			// Fixed parameters
			double clutches = 2.5;
			double hatchRate = 7.8;
			double eggOffTake = 2.86;
			double introConstant = 10;
			double localConstant = 2.;
			double transConstant = 1.35;

			// Starting values
			double transC = transD.sample() / (flockSize * tConstant);
			double chickDeathC = chickDeathD.sample();
			double growerDeathC = growerDeathD.sample();
			double adultDeathC = adultDeathD.sample();
			double growerSalesC = growerSalesD.sample();
			double adultSalesC = adultSalesD.sample();
			double localTransC = localTransD.sample();
			//double paraBucket[] = {chickDeathC, growerDeathC, adultDeathC, growerSalesC, adultSalesC, localTransC};
			
			double paraBucket[] = {0.0104, 0.001, 0.0004, 0.012, 0.0006, 0.344};
			transC = 3 / (18.91 * 2.5);
			UniformIntegerDistribution paraSampler = new UniformIntegerDistribution(0, 6);
			
			double currVal = 100000000;
			double adjFactor = 1.05;

			String file = "Outputs\\Africa\\Tanzania\\Tanzania_ABC_s2.txt";
			ReadWrite writer = new ReadWrite(file);
//			Create a high path ND virus			
			int count = 0;
			int minCount = 0;
			int currPara = 0;
			double previousVal = 0;
			
			double[] minBucket = paraBucket.clone();
			double minTrans = transC;
			double minEst = currVal;
			
			while(count < 100000){
				
				if(count > 0){
					currPara = paraSampler.sample();
					if(currPara < 6){
						previousVal = paraBucket[currPara];
						paraBucket[currPara] = distArray[currPara].sample();
					}
					if(currPara == 6){
						previousVal = transC;
						transC = transD.sample() / (flockSize * tConstant);					
					}
				}
				NDInfection hpndv = new NDInfection(this.NDPath, this.latent, this.infectious, this.prot, this.titre, this.maternalA, this.mortality, transC);
				double[] paramsB = {chicks + growers, hens, cocks, paraBucket[0], paraBucket[1], paraBucket[2], clutches, hatchRate, eggOffTake, paraBucket[3], paraBucket[4], 0.003};
			
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(1, 200, paramsB);
//			Force the trojan flock into the model
//				Flock trojan = new Flock(paramsMP, true);
				int nTrojans = 40;
				Flock[] trojan = trojans(paramsB, nTrojans);
				test.forceTrojan(trojan);
				test.setHenCockRatio(3.);
				test.setMaxChickAge(56);
				test.setMaxGrowerAge(180);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				double tSales = 0;
				double tChicks = 0;
				double tGrowers = 0;
				double tHens = 0;
				double tCocks = 0;
				double ndDeaths = 0;
				double nonNDDeaths = 0;
				boolean flag = false;
				int nYear = 10;
				for(int i = 0; i < 365 * (3 + nYear); i++){
//			Define the seasonality of the transmission in Madhya Pradesh

					if(i == 365 * 2) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, paraBucket[5], 1);
					else if(i > (365 * 2) && (i - 270) % 365 == 0) test.setNDVs(0.000001, paraBucket[5] / localConstant, 1 / transConstant);					

					test.stepPopulation(i);
					Flock[] trojanArrays = test.getTrojan(nTrojans);
					for(int k = 0; k < trojanArrays.length; k++){
					Flock anni = trojanArrays[k];
						if(anni.getSize() > 110){
						i = 10000;
						flag = true;
					}
					if(i > 365 * 3){
						tSales = tSales + ((double)(anni.getSaleOutput()[0] + anni.getSaleOutput()[1]) / nTrojans);
						tChicks = tChicks + ((double) anni.nChicksI / nTrojans);
						tGrowers = tGrowers + ((double) anni.nGrowersI / nTrojans);
						tHens = tHens + ((double) anni.nHensI / nTrojans);
						//System.out.println(tHens);
						tCocks = tCocks + ((double) anni.nCocks / nTrojans);
						ndDeaths = ndDeaths + ((double) anni.getndDeaths()[0] / nTrojans);
						nonNDDeaths = nonNDDeaths + ((double) anni.getTotalDeaths() / nTrojans);
						}
					}
				}
				double chickDiff = (this.mod(((tChicks / (365*nYear)) - chicks)) / chicks) * dWeight;
				double growerDiff = (this.mod(((tGrowers / (365*nYear)) - growers)) / growers) * dWeight;
				double henDiff = (this.mod(((tHens / (365*nYear)) - hens)) / hens) * hWeight;
				double cockDiff = (this.mod(((tCocks / (365*nYear)) - cocks)) / cocks) * cWeight;
				double salesDiff = (this.mod(((tSales / nYear) - birdOT)) / birdOT) * otWeight;
				double ndDiff = this.mod(ndDeathsO - (ndDeaths / (ndDeaths + nonNDDeaths))) * deathWeight;
				double deathDiff = (this.mod(deaths - ((ndDeaths + nonNDDeaths) / nYear)) / deaths) * deathWeightA;
			
				double est = chickDiff + growerDiff + henDiff + cockDiff + salesDiff + ndDiff + deathDiff;
				if(est > currVal * adjFactor | flag) {
					if(currPara < 6) paraBucket[currPara]= previousVal;
					if(currPara == 6) transC = previousVal;
				}
				
				if(est < minEst * adjFactor){
					currVal = est;
					Date date = new Date();
					System.out.println(count + " " + est + " " + transC + " " + this.printParas(paraBucket) + " " + date.toString());
					System.out.println(count + " " + tSales/10 + " " + tChicks/3650 + " " + tGrowers/3650 + " "+ tHens/3650 + " " + tCocks/3650 + " "+ ndDeaths/10+ " "+ nonNDDeaths/10);
					writer.openWriteABC(count + " " + est + " " + transC + " " + this.printParas(paraBucket));
				}
				if(est < minEst){
					minCount = count;
					minBucket = paraBucket.clone();
					minTrans = transC;
					minEst = est;
				}
				count ++;
				
				if(count - 100 > minCount){
					currVal = minEst;
					distArray = this.changeDists(distArray.clone(), minBucket.clone());
					paraBucket = minBucket.clone();
					transD = new NormalDistribution (minTrans * (flockSize * tConstant), transD.getStandardDeviation() / 2);
					transC = minTrans;
					minCount = count;
				}
			}
		}

		private double mod(double number){
			return Math.sqrt(Math.pow(number, 2));
		}
		
		private String printParas(double[] bucket){
			return(bucket[0] + " " + bucket[1] + " " + bucket[2] + " " + bucket[3] + " " + bucket[4] + " " + bucket[5]);
		}
		
		private UniformRealDistribution[] changeDists(UniformRealDistribution[] paraDists, double[] newVals){
			UniformRealDistribution[] outputDists = paraDists;
			for(int i = 0; i < outputDists.length; i++){
				double currRange = outputDists[i].getSupportUpperBound() - outputDists[i].getSupportLowerBound();
				outputDists[i] = new UniformRealDistribution(newVals[i] - (currRange / 4), newVals[i] + (currRange / 4));
			}
			if(outputDists[0].getSupportLowerBound() < 0.005){
				outputDists[0] = new UniformRealDistribution(0.005, outputDists[0].getSupportUpperBound());			
			}
			if(outputDists[2].getSupportLowerBound() < 0.0002){
				outputDists[2] = new UniformRealDistribution(0.0002, outputDists[2].getSupportUpperBound());			
			}
			if(outputDists[4].getSupportLowerBound() < 0.0001){
				outputDists[4] = new UniformRealDistribution(0.0001, outputDists[5].getSupportUpperBound());			
			}

			return outputDists;
		}
		
		private Flock[] trojans(double[] params, int n){
			Flock[] output = new Flock[n];
			for(int i = 0; i < n; i++) output[i] = new Flock(params, true, 1);
			return output;
		}
	
}
