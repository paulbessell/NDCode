/*********************************
 * 
 * driver.java contains methods for implementing the Newcastle Disease model
 * 
 * 
 ********************************/


import java.util.*;

import org.apache.commons.math3.distribution.BinomialDistribution;

public class driver {

	public static void main(String[] args) throws Exception{
// 		TODO Auto-generated method stub
		
//		Call and run the required method 
//		System.out.println(new BinomialDistribution(1, 0.2).sample());
		driver myDriver = new driver();			
		//myDriver.runPopulationOrissaVacc();
//		myDriver.runPopulationMPTrojan();
//		myDriver.runPopulationHaryanaTrojan();
//		myDriver.runPopulationOrissaTrojan();
//		myDriver.runPopulationHaryanaTrojanVacc();
//		myDriver.runPopulationMPTrojanVacc();
//		myDriver.runPopulationOrissaTrojanVacc();
//		myDriver.runPopulationMPBigTrojanVacc();
//		myDriver.runPopulationMPBigTrojanVacc4Y();
		
//		myDriver.runPopulationHaryanaBigTrojanVacc();
//		myDriver.runPopulationHaryanaBigTrojanVacc4Y();
	
//		myDriver.runPopulationOrissaBigBaseline();
/*		myDriver.runPopulationOrissaBigTrojanVacc();
		myDriver.runPopulationOrissaBigTrojanVacc4Y();
	*/
	//	myDriver.runHaryanaInfographicBaseline();
	//	myDriver.runHaryanaInfographicVaccine();
	//	myDriver.runHaryanaInfographicVaccineIntro();
	//	myDriver.runOrissaaInfographicBaseline();
	//	myDriver.runOrissaaInfographicVaccine();
	//	myDriver.runHaryanaInfographicVaccineIntro();
		//		myDriver.runPopulationHaryanaBigBaseline();
//		myDriver.runPopulationMPBigBaseline();
		myDriver.runHaryanaInfographicBaselineOnOff();
		myDriver.runHaryanaInfographicVaccineOnOff();
	}
	
	public driver(){}

//		A simple method to test ND in a single flock	
	public void runSingleFlock() throws Exception{
//		Define the parameters for the ND virus(es)		
		String NDPath = "Low path";
		int latent = 5;
		int infectious = 5;
		int prot = 105;
		int titre = 300;
		int maternalA = 42;
		double mortality = 0;
		double transmission = 2.2/150;
		NDInfection ndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
		String file = "Outputs\\TestSingleDists.csv";
//		Initiating the writing of the model 
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
//		Loop through the number of iterations
		for(int h = 1; h < 1000; h++){
		Flock test = new Flock();
//		Define an initial number of infected birds
		int nSeed = 2;
		if(test.getSize() < 2) nSeed = test.getSize();
		test.seedND(2, ndv);

		for(int i = 0; i < 3650; i++){
//		Set seasonal adjustment
			test.getSeasonAdj(i);
//		Time step the demography
			test.timeStep();
//		Time step Newcastle Disease
			test.stepND();
//		Time step random "sparks" of infection
			test.seedNDSpark(ndv, 0.001);
			int[] ndOut = test.getNDComps();
			writer.writemodel(h,i,ndOut);
		}
		System.out.println(h);
		}
	}

//		Runs just the demographic component of a single flock
	public void runSingleFlockDemog() throws Exception{
		String file = "Outputs\\TestDemogSeasonRevTest.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWriteDemog();
		for(int h = 1; h < 500; h++){
		Flock test = new Flock();

		for(int i = 0; i < 3650; i++){
			test.getSeasonAdj(i);
			int[] sales = test.timeStep();
			int[] flockOut = test.getFlock();
			writer.writeDemog(h,i,flockOut, sales);
		}
		System.out.println(h);
		}
	}

//		This method tests ND within a single village - multiple flocks
	public void runSingleVillage() throws Exception{
		String NDPath = "Low path";
		int latent = 5;
		int infectious = 5;
		int prot = 90;
		int titre = 300;
		int maternalA = 42;
		double mortality = 0;
		double transmission = 1.1/165;
		NDInfection ndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
		Village test = new Village(50);
		test.seedND(ndv);
		test.writeFlockSizes();
//		Define the local transmission parameter
		test.setLocalTransmission(0.00001);
		
		String file = "Outputs\\TestSingleVillage.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
//		Step through the days
		for(int i = 0; i < 3650; i++){
			test.stepVillage(i);
			writer.writemodel(1, i, test.getComps());
		}
	}

//		Run a population of villages, this includes both high path and low path ND viruses
	public void runPopulation()throws Exception{
		String NDPath = "Low path";
		int latent = 5;
		int infectious = 5;
		int prot = 105;
		prot = 90;
		int titre = 300;
		int maternalA = 42;
		double mortality = 0;
		double transmission = 1.8/165;
		NDInfection ndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);

		NDPath = "High path";
		latent = 5;
		infectious = 5;
		prot = 105;
		titre = 300;
		maternalA = 42;
		mortality = 0.6;
		transmission = 2.2/165;
		NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);

		String file = "Outputs\\TestSinglePopulation2Virus3.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
		for(int k = 0; k < 100; k++){
			Population test = new Population(50);
			test.seedND(ndv);
			test.seedND(hpndv);
			test.setNDVs(0.000025);

 //		This changes the local transmission parameter after one year when the 
 //		disease has become established in the modelled population			
		for(int i = 0; i < 365 * 10; i++){
			if(i == 365) test.setNDVs(0.000005);
			test.stepPopulation(i);
			writer.writemodel(k, i, test.getComps());
		}
			System.out.println("Iteration = "+k);
		}
	}

//		Run vaccination in a populaiton of villages	- again two viruses here
	public void runPopulationVaccination()throws Exception{
		String NDPath = "Low path";
		int latent = 5;
		int infectious = 5;
		int prot = 105;
		prot = 90;
		int titre = 300;
		int maternalA = 42;
		double mortality = 0;
		double transmission = 1.7/165;
		NDInfection ndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);

		NDPath = "High path";
		latent = 5;
		infectious = 5;
		prot = 105;
		titre = 300;
		maternalA = 42;
		mortality = 0.68;
		transmission = 2.3/165;
		NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);

//		Create the vaccine		
		NDInfection vaccine = new NDInfection(90);
		String file = "Outputs\\Vaccine\\vaccine05_3.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
		for(int k = 0; k < 100; k++){
			Population test = new Population(50);
			test.seedND(ndv);
			test.seedND(hpndv);
			test.setNDVs(0.000025);
			for(int i = 0; i < 365 * 10; i++){
				if(i == 365) test.setNDVs(0.000005);
				if(i == 365*3) test.startVaccination(vaccine, 0.05, i,0.8837, 121);
				if(i >= 365*3) test.vaccinate(i);
			test.stepPopulation(i);
			writer.writemodel(k, i, test.getComps());
		}
			System.out.println("Iteration = "+k);
		}
	}
	
//		 Driver methods for three states in India - just the flock demographics
	public void runSingleFlockDemogIndia() throws Exception{
		String file = "Outputs\\India\\TestDemogMP_3.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWriteDemog();
		
//		This defines parameters for each state based on values fitted using the code below
		double[] paramsMP = {4.21, 2.35, 1.4, 0.05, 0.0015, 0.0007, 3.1, 7.2, 1.6, 0.019, 0.0009, 0.001};
		double[] paramsH = {1.83, 1.6, 1.02, 0.04, 0.001, 0.0005, 3.8, 6.6, 3.8, 0.017, 0.00085, 0.001};
		double[] paramsO = {3.8, 2.03, 1.31, 0.03, 0.0015, 0.0005, 2.9, 5.9, 2.9, 0.019, 0.00085, 0.001};
		
		
		for(int h = 1; h < 100; h++){
		Flock test = new Flock(paramsMP, false, 1);

		for(int i = 0; i < 3650; i++){
		//	test.getSeasonAdj(i);
			int[] sales = test.timeStep();
			int[] flockOut = test.getFlock();
//			System.out.println("S = " + ndOut[0] + " E = " + ndOut[1]+ " I = " + ndOut[2] + " P = " + ndOut[3] + " R = "+ ndOut[4]);
			writer.writeDemog(h,i,flockOut, sales);
		}
		System.out.println(h);
		}
	}
	

//		Run ND in Madhya Pradesh with a single "trojan" flock that is used to monitor a single flock
		public void runPopulationMPTrojan() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
//			Maternal antibodies. This assumption has been left out
			int maternalA = 42;
//			Daily mortality rate
			double mortality = 0.85/5;
			double transmission = 3/(7.96*2.5);
			transmission = 0.1554818;
			double lTrans = 0.24653;
//			Create a high path ND virus
			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);

			double ndA = 0.5;
			double prodImp = 1.1;
			//double[] paramsMP = {4.21, 2.35, 1.4, 0.05 * 0.675, 0.0015 * ndA, 0.0007 * ndA, 3.1, 7.2, 1.6, 0.019 * 0.7, 0.0009, 0.001};
			double[] paramsMP =  {4.21, 2.35, 1.4, 0.042170, 0.00054416, 0.0003893, 3.1, 7.2, 1.6, 0.0140497, 0.0002583, 0.001};
			
//			This writes the output for a single "trojan" flock
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\MadhyaPradesh\\MP_Baseline_Rev_10P.csv";
			ReadWrite demogWriter = new ReadWrite(file);
			demogWriter.openWriteDemogND();

//			This writes the overall situation in the population
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\MadhyaPradesh\\MP_Baseline_Rev_10P.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			
			for(int k = 0; k < 82; k++){
//			Implement a population of 10 villages with 110 flocks per village
				Population test = new Population(10, 110, paramsMP);
//			Force the trojan flock into the model
				Flock trojan = new Flock(paramsMP, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
//			Define the seasonality of the transmission in Madhya Pradesh
/*					if(i == 365 * 2) test.setNDVs(0.00001, 0.1175, 0.875);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, 0.24, 1.1);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, 0.1175, 0.875);
*/
					if(i == 365 * 2) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int[] ndDeathReason = anni.getndDeathsReason();
				int[] demProtected = anni.getProtected();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
				boolean vaccD = false; 
				demogWriter.writeDemogND(k,i,flockOut, births, deaths, ndDeaths, ndDeathReason, demProtected, sales, graduates, vaccD);
			}
				System.out.println("MP Season Iteration = "+k);
			}

		}

		public void runPopulationHaryanaTrojan() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			//Haryana mortality
			double mortality = 0.85/5;
			double transmission = 3/(4.45*2.5);
			transmission = 0.3545;
			double lTrans = 0.328;
			
			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);

			// ND adjustment
			double ndA = 0.7;
			
			double[] paramsH = {1.83, 1.6, 1.02, 0.04*0.9, 0.001 * ndA, 0.0005 * ndA, 3.8, 6.6, 3.8, 0.017 * 0.8, 0.00085 * 0.8, 0.001};
			paramsH = new double[] {1.83, 1.6, 1.02, 0.032863, 0.000628, 0.000288, 3.8, 6.6, 3.8, 0.014111, 0.000493, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Haryana\\Haryana_Baseline_Rev.csv";
			ReadWrite demogWriter = new ReadWrite(file);
			demogWriter.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\Haryana\\Haryana_Baseline_Rev.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();

			double localConstantA = 3;
			double transConstantA = 1.35;
			double localConstantB = 2;
			double transConstantB = 1.15;

			for(int k = 0; k < 138; k++){
				Population test = new Population(5, 312, paramsH);
				Flock trojan = new Flock(paramsH, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
/*					if(i == 365 * 2) test.setNDVs(0.000001, 0.1, 0.8);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, 0.3, 1.1);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, 0.1, 0.8);
					else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, 0.15, 0.95);
					else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, 0.1, 0.8);
*/
					if(i == 365 * 2) test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, lTrans/localConstantB, 1 / transConstantB);
					else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);

				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int[] ndDeathReason = anni.getndDeathsReason();
				int[] demProtected = anni.getProtected();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
				boolean vaccD = false;
				demogWriter.writeDemogND(k,i,flockOut, births, deaths, ndDeaths, ndDeathReason, demProtected, sales, graduates, vaccD);
			}
				System.out.println("Haryana Season Iteration = "+k);
			}

		}
	
//		Model for Orissa again
		public void runPopulationOrissaTrojan() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			double mortality = 0.85 / 5;
			// Adjusted transmission for Orissa
			double transmission = 3/(7.15*2.5);
			
			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
//		Flock parameters for Orissa 
			double[] paramsO = {3.8, 2.03, 1.31, 0.03, 0.0015, 0.0005, 2.9, 5.9, 2.9, 0.019, 0.00085, 0.001};

			// ND adjustment
			double ndA = 0.6;
			
			paramsO = new double[]{3.8, 2.03, 1.31, 0.03 * 0.6, 0.0015 * ndA, 0.0005 * ndA, 2.9, 5.9, 2.9, 0.019 * 0.65, 0.00085 * 0.65, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Orissa\\Orissa_Baseline_v2.csv";
			ReadWrite demogWriter = new ReadWrite(file);
			demogWriter.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\Orissa\\Orissa_Baseline_v2.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 165; k++){
				Population test = new Population(5, 332, paramsO);
				Flock trojan = new Flock(paramsO, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
					if(i == 365 * 2) test.setNDVs(0.000001, 0.08, 0.8);
//					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, 0.24, 1.1);
//					else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, 0.08, 0.8);					
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, 0.26, 1.1);
					else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, 0.1, 0.8);					
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int[] ndDeathReason = anni.getndDeathsReason();
				int[] demProtected = anni.getProtected();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
				boolean vaccD = false; 
				demogWriter.writeDemogND(k,i,flockOut, births, deaths, ndDeaths, ndDeathReason, demProtected, sales, graduates, vaccD);
			}
				System.out.println("Orissa Season Iteration = "+k);
			}
		}
		
//		Implement vaccination in these systems, with a vaccinated trojan flock
		public void runPopulationHaryanaTrojanVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			//Haryana mortality
			double mortality = 0.85/5;
			double transmission = 3/(4.45*2.5);
			transmission = 0.3545;
			double lTrans = 0.328;
			
			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			// ND adjustment
			double ndA = 0.7;
			
			double[] paramsH = {1.83, 1.6, 1.02, 0.04*0.9, 0.001 * ndA, 0.0005 * ndA, 3.8, 6.6, 3.8, 0.017 * 0.8, 0.00085 * 0.8, 0.001};
			paramsH = new double[] {1.83, 1.6, 1.02, 0.032863, 0.000628, 0.000288, 3.8, 6.6, 3.8, 0.014111, 0.000493, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Haryana\\Haryana_VaccRev5.csv";
			ReadWrite demogWriter = new ReadWrite(file);
			demogWriter.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\Haryana\\Haryana_VaccRev5.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();

			double localConstantA = 3;
			double transConstantA = 1.35;
			double localConstantB = 2;
			double transConstantB = 1.15;

			for(int k = 0; k < 200; k++){
				Population test = new Population(5, 312, paramsH);
				Flock trojan = new Flock(paramsH, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
/*					if(i == 365 * 2) test.setNDVs(0.000001, 0.1, 0.8);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, 0.3, 1.1);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, 0.1, 0.8);
					else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, 0.15, 0.95);
					else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, 0.1, 0.8);
	*/				
					if(i == 365 * 2) test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, lTrans/localConstantB, 1 / transConstantB);
					else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);

					if(i == 365*4){
//		Define a vaccination of 10% of flocks with an efficacy of 0.8837
						test.startVaccination(vaccine, 0.05, i, 0.8837,121);
						test.startVaccinationTrojan(vaccine, i, 0.8837);
					//	test.setProdImprovement(1.2);
					//	test.setElasticity(1.5);

					}
					if(i >= 365*4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int[] ndDeathReason = anni.getndDeathsReason();
				int[] demProtected = anni.getProtected();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
				boolean vaccD = anni.getVaccDay() == i; 
				demogWriter.writeDemogND(k,i,flockOut, births, deaths, ndDeaths, ndDeathReason, demProtected, sales, graduates, vaccD);
			}
				System.out.println("Haryana Vaccination Season Iteration = "+k);
			}
		}
		
		public void runPopulationMPTrojanVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(7.96*2.5);
			transmission = 0.1554818;
			double lTrans = 0.24653;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsMP = {4.21, 2.35, 1.4, 0.05, 0.0015, 0.0007, 3.1, 7.2, 1.6, 0.019, 0.0009, 0.001};
			double[] paramsH = {1.83, 1.6, 1.02, 0.04, 0.001, 0.0005, 3.8, 6.6, 3.8, 0.017, 0.00085, 0.001};
			double[] paramsO = {3.8, 2.03, 1.31, 0.03, 0.0015, 0.0005, 2.9, 5.9, 2.9, 0.019, 0.00085, 0.001};

			double ndA = 0.5;
			paramsMP = new double[]{4.21, 2.35, 1.4, 0.05 * 0.675, 0.0015 * ndA, 0.0007 * ndA, 3.1, 7.2, 1.6, 0.019 * 0.7, 0.0009, 0.001};
			paramsMP =  new double[]{4.21, 2.35, 1.4, 0.042170, 0.00054416, 0.0003893, 3.1, 7.2, 1.6, 0.0140497, 0.0002583, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\MadhyaPradesh\\MP_VaccRev10EP20.csv";
			ReadWrite demogWriter = new ReadWrite(file);
			demogWriter.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\MadhyaPradesh\\MP_VaccRev10EP20.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 200; k++){
				Population test = new Population(10, 110, paramsMP);
				Flock trojan = new Flock(paramsMP, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
					/*					if(i == 365 * 2) test.setNDVs(0.00001, 0.1175, 0.875);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, 0.24, 1.1);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, 0.1175, 0.875);
*/
					if(i == 365 * 2) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
					if(i == 365*4){
						test.startVaccination(vaccine, 0.1, i, 0.8837, 121);
						test.startVaccinationTrojan(vaccine, i, 0.8837);
						test.setProdImprovement(1.2);
						test.setElasticity(1.5);

					}
					if(i >= 365*4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int[] ndDeathReason = anni.getndDeathsReason();
				int[] demProtected = anni.getProtected();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
				boolean vaccD = anni.getVaccDay() == i; 
				demogWriter.writeDemogND(k,i,flockOut, births, deaths, ndDeaths, ndDeathReason, demProtected, sales, graduates, vaccD);
			}
				System.out.println("MP Season Iteration = "+k);
			}

		}
		public void runPopulationOrissaTrojanVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			//Orissa mortality
			double mortality = 0.85/5;
//			double transmission = 1.9/(7.15*5);
			double transmission = 3/(7.15*2.5);

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			// ND adjustment - Orissa
			double ndA = 0.6;	
			double[] paramsO = new double[]{3.8, 2.03, 1.31, 0.03 * 0.6, 0.0015 * ndA, 0.0005 * ndA, 2.9, 5.9, 2.9, 0.019 * 0.65, 0.00085 * 0.65, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Orissa\\Orissa_Vacc70.csv";
			ReadWrite demogWriter = new ReadWrite(file);
			demogWriter.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\Orissa\\Orissa_Vacc70.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 200; k++){
				Population test = new Population(5, 332, paramsO);
				Flock trojan = new Flock(paramsO, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
					if(i == 365 * 2) test.setNDVs(0.000001, 0.08, 0.8);
				// Tweaked from 0.22 and 1.0
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, 0.26, 1.1);
					else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, 0.1, 0.8);					
					if(i == 365 * 4){
						test.startVaccination(vaccine, 0.7, i, 0.8837, 121);
						test.startVaccinationTrojan(vaccine, i, 0.8837);
					//	test.setElasticity(1.5);
					}
					if(i >= 365 * 4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int[] ndDeathReason = anni.getndDeathsReason();
				int[] demProtected = anni.getProtected();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
				boolean vaccD = anni.getVaccDay() == i; 
				demogWriter.writeDemogND(k,i,flockOut, births, deaths, ndDeaths, ndDeathReason, demProtected, sales, graduates, vaccD);
			}
				System.out.println("Orissa Vaccination Season Iteration = "+k);
			}
		}
		public void runPopulationMPBigTrojanVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(7.96*2.5);
			transmission = 0.1554818;
			double lTrans = 0.24653;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsMP = {4.21, 2.35, 1.4, 0.05, 0.0015, 0.0007, 3.1, 7.2, 1.6, 0.019, 0.0009, 0.001};

			double ndA = 0.5;
			paramsMP =  new double[]{4.21, 2.35, 1.4, 0.042170, 0.00054416, 0.0003893, 3.1, 7.2, 1.6, 0.0140497, 0.0002583, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\MadhyaPradesh\\Single\\MPVacc10EP10.csv";
			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\MadhyaPradesh\\Double\\MPVacc10EP10.csv";
			ReadWrite demogWriter2 = new ReadWrite(file);
			demogWriter2.openWriteDemogND();

			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\MadhyaPradesh\\Triple\\MPVacc10EP10.csv";
			ReadWrite demogWriter3 = new ReadWrite(file);
			demogWriter3.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\MadhyaPradesh\\Combined\\MPVacc10EP10.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 200; k++){
				Population test = new Population(5, 110, paramsMP);
				Flock trojan = new Flock(paramsMP, true, 1);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsMP, true, 2);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsMP, true, 3);
				test.forceTrojan(trojan);

				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
					if(i == 365 * 2) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
					if(i == 365*4){
						test.startVaccination(vaccine, 0.1, i, 0.8837, 121);
						test.startVaccinationTrojans(vaccine, i, 0.8837, 3);
						test.setProdImprovement(1.1);
						test.setElasticity(1.5);

					}
					if(i >= 365*4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock[] anni = test.getTrojan(3);
				demogWriter1.writeTrojan(k,i,anni[0]);
				demogWriter2.writeTrojan(k,i,anni[1]);
				demogWriter3.writeTrojan(k,i,anni[2]);

				//demogWriter.writeDemogND(k,i,flockOut, births, deaths, ndDeaths, ndDeathReason, demProtected, sales, graduates, vaccD);
			}
				System.out.println("MP Season Iteration = "+k);
			}

		}
		public void runPopulationMPBigTrojanVacc4Y() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(7.96*2.5);
			transmission = 0.1554818;
			double lTrans = 0.24653;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsMP = {4.21, 2.35, 1.4, 0.05, 0.0015, 0.0007, 3.1, 7.2, 1.6, 0.019, 0.0009, 0.001};

			double ndA = 0.5;
			paramsMP =  new double[]{4.21, 2.35, 1.4, 0.042170, 0.00054416, 0.0003893, 3.1, 7.2, 1.6, 0.0140497, 0.0002583, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\MadhyaPradesh\\Single\\MPVacc10EP204PY.csv";
			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\MadhyaPradesh\\Double\\MPVacc10EP204PY.csv";
			ReadWrite demogWriter2 = new ReadWrite(file);
			demogWriter2.openWriteDemogND();

			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\MadhyaPradesh\\Triple\\MPVacc10EP204PY.csv";
			ReadWrite demogWriter3 = new ReadWrite(file);
			demogWriter3.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\MadhyaPradesh\\Combined\\MPVacc10EP204PY.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 200; k++){
				Population test = new Population(5, 110, paramsMP);
				Flock trojan = new Flock(paramsMP, true, 1);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsMP, true, 2);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsMP, true, 3);
				test.forceTrojan(trojan);

				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
					if(i == 365 * 2) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
					if(i == 365*4){
						test.startVaccination(vaccine, 0.1, i, 0.8837, 92);
						test.startVaccinationTrojans(vaccine, i, 0.8837, 3);
						test.setProdImprovement(1.2);
						test.setElasticity(1.5);

					}
					if(i >= 365*4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock[] anni = test.getTrojan(3);
				demogWriter1.writeTrojan(k,i,anni[0]);
				demogWriter2.writeTrojan(k,i,anni[1]);
				demogWriter3.writeTrojan(k,i,anni[2]);

				//demogWriter.writeDemogND(k,i,flockOut, births, deaths, ndDeaths, ndDeathReason, demProtected, sales, graduates, vaccD);
			}
				System.out.println("MP Season Iteration = "+k);
			}

		}

		public void runPopulationHaryanaBigTrojanVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(4.45*2.5);
			transmission = 0.3545;
			double lTrans = 0.328;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsH = {1.83, 1.6, 1.02, 0.032863, 0.000628, 0.000288, 3.8, 6.6, 3.8, 0.014111, 0.000493, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Haryana\\Single\\HaryanaVacc10EP10.csv";
			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Haryana\\Double\\HaryanaVacc10EP10.csv";
			ReadWrite demogWriter2 = new ReadWrite(file);
			demogWriter2.openWriteDemogND();

			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Haryana\\Triple\\HaryanaVacc10EP10.csv";
			ReadWrite demogWriter3 = new ReadWrite(file);
			demogWriter3.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\Haryana\\Combined\\HaryanaVacc10EP10.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			
			double localConstantA = 3;
			double transConstantA = 1.35;
			double localConstantB = 2;
			double transConstantB = 1.15;

			for(int k = 0; k < 200; k++){
				Population test = new Population(1, 312, paramsH);
				Flock trojan = new Flock(paramsH, true, 1);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsH, true, 2);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsH, true, 3);
				test.forceTrojan(trojan);

				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
					if(i == 365 * 2) test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, lTrans/localConstantB, 1 / transConstantB);
					else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);

					if(i == 365*4){
//		Define a vaccination of 10% of flocks with an efficacy of 0.8837
						test.startVaccination(vaccine, 0.1, i, 0.8837,121);
						test.startVaccinationTrojans(vaccine, i, 0.8837,3);
						test.setProdImprovement(1.1);
						test.setElasticity(1.5);

					}
					if(i >= 365*4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock[] anni = test.getTrojan(3);
				demogWriter1.writeTrojan(k,i,anni[0]);
				demogWriter2.writeTrojan(k,i,anni[1]);
				demogWriter3.writeTrojan(k,i,anni[2]);

			}
				System.out.println("Haryana Season Iteration = "+k);
			}

		}
		public void runPopulationHaryanaBigTrojanVacc4Y() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(4.45*2.5);
			transmission = 0.3545;
			double lTrans = 0.328;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsH = {1.83, 1.6, 1.02, 0.032863, 0.000628, 0.000288, 3.8, 6.6, 3.8, 0.014111, 0.000493, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Haryana\\Single\\HaryanaVacc10EP104PY.csv";
			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Haryana\\Double\\HaryanaVacc10EP104PY.csv";
			ReadWrite demogWriter2 = new ReadWrite(file);
			demogWriter2.openWriteDemogND();

			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Haryana\\Triple\\HaryanaVacc10EP104PY.csv";
			ReadWrite demogWriter3 = new ReadWrite(file);
			demogWriter3.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\Haryana\\Combined\\HaryanaVacc10EP104PY.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			
			double localConstantA = 3;
			double transConstantA = 1.35;
			double localConstantB = 2;
			double transConstantB = 1.15;

			for(int k = 0; k < 200; k++){
				Population test = new Population(1, 312, paramsH);
				Flock trojan = new Flock(paramsH, true, 1);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsH, true, 2);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsH, true, 3);
				test.forceTrojan(trojan);

				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
					if(i == 365 * 2) test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, lTrans/localConstantB, 1 / transConstantB);
					else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);

					if(i == 365*4){
//		Define a vaccination of 10% of flocks with an efficacy of 0.8837
//						test.startVaccination(vaccine, 0.1, i, 0.8837,121);
						test.startVaccination(vaccine, 0.1, i, 0.8837,92);
						test.startVaccinationTrojans(vaccine, i, 0.8837,3);
						test.setProdImprovement(1.1);
						test.setElasticity(1.5);

					}
					if(i >= 365*4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock[] anni = test.getTrojan(3);
				demogWriter1.writeTrojan(k,i,anni[0]);
				demogWriter2.writeTrojan(k,i,anni[1]);
				demogWriter3.writeTrojan(k,i,anni[2]);

				//demogWriter.writeDemogND(k,i,flockOut, births, deaths, ndDeaths, ndDeathReason, demProtected, sales, graduates, vaccD);
			}
				System.out.println("Haryana Season Iteration = "+k);
			}

		}
		public void runPopulationOrissaBigTrojanVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(7.16*2.5);
			transmission = 0.2639;
			double lTrans = 0.3966;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsO = {3.8, 2.03, 1.31, 0.0055, 0.0005, 0.000323, 2.9, 5.9, 2.9, 0.01799, 0.000129, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Orissa\\Single\\OrissaVacc10.csv";
			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Orissa\\Double\\OrissaVacc10.csv";
			ReadWrite demogWriter2 = new ReadWrite(file);
			demogWriter2.openWriteDemogND();

			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Orissa\\Triple\\OrissaVacc10.csv";
			ReadWrite demogWriter3 = new ReadWrite(file);
			demogWriter3.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\Orissa\\Combined\\OrissaVacc10.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			
			double introConstant = 10;
			double localConstant = 2.6;
			double transConstant = 1.35;

			for(int k = 0; k < 200; k++){
				Population test = new Population(1, 332, paramsO);
				Flock trojan = new Flock(paramsO, true, 1);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsO, true, 2);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsO, true, 3);
				test.forceTrojan(trojan);

				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){

					if(i == 365 * 2) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, lTrans, 1);
					else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);					

					if(i == 365*4){
//		Define a vaccination of 10% of flocks with an efficacy of 0.8837
						test.startVaccination(vaccine, 0.1, i, 0.8837,121);
						test.startVaccinationTrojans(vaccine, i, 0.8837,3);
//						test.setProdImprovement(1.2);
//						test.setElasticity(1.5);
					}
					if(i >= 365*4) test.vaccinate(i);
				
					test.stepPopulation(i);
					writer.writemodel(k, i, test.getComps());
					Flock[] anni = test.getTrojan(3);
					demogWriter1.writeTrojan(k,i,anni[0]);
					demogWriter2.writeTrojan(k,i,anni[1]);
					demogWriter3.writeTrojan(k,i,anni[2]);
				}
				System.out.println("Orissa Season Iteration = "+k);
			}
		}
		public void runPopulationOrissaBigTrojanVacc4Y() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(7.16*2.5);
			transmission = 0.2639;
			double lTrans = 0.3966;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsO = {3.8, 2.03, 1.31, 0.0055, 0.0005, 0.000323, 2.9, 5.9, 2.9, 0.01799, 0.000129, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Orissa\\Single\\OrissaVacc104PY.csv";
			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Orissa\\Double\\OrissaVacc104PY.csv";
			ReadWrite demogWriter2 = new ReadWrite(file);
			demogWriter2.openWriteDemogND();

			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Orissa\\Triple\\OrissaVacc104PY.csv";
			ReadWrite demogWriter3 = new ReadWrite(file);
			demogWriter3.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\Orissa\\Combined\\OrissaVacc104PY.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			
			double introConstant = 10;
			double localConstant = 2.6;
			double transConstant = 1.35;

			for(int k = 0; k < 200; k++){
				Population test = new Population(1, 332, paramsO);
				Flock trojan = new Flock(paramsO, true, 1);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsO, true, 2);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsO, true, 3);
				test.forceTrojan(trojan);

				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){

					if(i == 365 * 2) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, lTrans, 1);
					else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);					

					if(i == 365*4){
//		Define a vaccination of 10% of flocks with an efficacy of 0.8837
						test.startVaccination(vaccine, 0.1, i, 0.8837,92);
						test.startVaccinationTrojans(vaccine, i, 0.8837,3);
//						test.setProdImprovement(1.2);
//						test.setElasticity(1.5);
					}
					if(i >= 365*4) test.vaccinate(i);
				
					test.stepPopulation(i);
					writer.writemodel(k, i, test.getComps());
					Flock[] anni = test.getTrojan(3);
					demogWriter1.writeTrojan(k,i,anni[0]);
					demogWriter2.writeTrojan(k,i,anni[1]);
					demogWriter3.writeTrojan(k,i,anni[2]);
				}
				System.out.println("Orissa Season Iteration = "+k);
			}
		}

		public void runPopulationOrissaBigBaseline() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(7.16*2.5);
			transmission = 0.2639;
			double lTrans = 0.3966;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);

			double[] paramsO = {3.8, 2.03, 1.31, 0.0055, 0.0005, 0.000323, 2.9, 5.9, 2.9, 0.01799, 0.000129, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Orissa\\Single\\Orissa_Baseline.csv";
			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Orissa\\Double\\Orissa_Baseline.csv";
			ReadWrite demogWriter2 = new ReadWrite(file);
			demogWriter2.openWriteDemogND();

			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Orissa\\Triple\\Orissa_Baseline.csv";
			ReadWrite demogWriter3 = new ReadWrite(file);
			demogWriter3.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\Orissa\\Combined\\Orissa_Baseline.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			
			double introConstant = 10;
			double localConstant = 2.6;
			double transConstant = 1.35;

			for(int k = 0; k < 400; k++){
				Population test = new Population(1, 332, paramsO);
				Flock trojan = new Flock(paramsO, true, 1);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsO, true, 2);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsO, true, 3);
				test.forceTrojan(trojan);

				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){

					if(i == 365 * 2) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, lTrans, 1);
					else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);					
					
					test.stepPopulation(i);
					writer.writemodel(k, i, test.getComps());
					Flock[] anni = test.getTrojan(3);
					demogWriter1.writeTrojan(k,i,anni[0]);
					demogWriter2.writeTrojan(k,i,anni[1]);
					demogWriter3.writeTrojan(k,i,anni[2]);
				}
				System.out.println("ORissa Season Iteration = "+k);
			}

		}
		public void runPopulationMPBigBaseline() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(7.96*2.5);
			transmission = 0.1554818;
			double lTrans = 0.24653;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);

			double[] paramsMP = {4.21, 2.35, 1.4, 0.05, 0.0015, 0.0007, 3.1, 7.2, 1.6, 0.019, 0.0009, 0.001};

			double ndA = 0.5;
			paramsMP =  new double[]{4.21, 2.35, 1.4, 0.042170, 0.00054416, 0.0003893, 3.1, 7.2, 1.6, 0.0140497, 0.0002583, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\MadhyaPradesh\\Single\\MP_Baseline2.csv";
			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\MadhyaPradesh\\Double\\MP_Baseline2.csv";
			ReadWrite demogWriter2 = new ReadWrite(file);
			demogWriter2.openWriteDemogND();

			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\MadhyaPradesh\\Triple\\MP_Baseline2.csv";
			ReadWrite demogWriter3 = new ReadWrite(file);
			demogWriter3.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\MadhyaPradesh\\Combined\\MP_Baseline2.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 200; k++){
				Population test = new Population(5, 110, paramsMP);
				Flock trojan = new Flock(paramsMP, true, 1);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsMP, true, 2);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsMP, true, 3);
				test.forceTrojan(trojan);

				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){

					if(i == 365 * 2) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
					
					test.stepPopulation(i);
					writer.writemodel(k, i, test.getComps());
					Flock[] anni = test.getTrojan(3);
					demogWriter1.writeTrojan(k,i,anni[0]);
					demogWriter2.writeTrojan(k,i,anni[1]);
					demogWriter3.writeTrojan(k,i,anni[2]);
				}
				System.out.println("MP Season Iteration = "+k);
			}

		}
		public void runPopulationHaryanaBigBaseline() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(4.45*2.5);
			transmission = 0.3545;
			double lTrans = 0.328;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsH = {1.83, 1.6, 1.02, 0.032863, 0.000628, 0.000288, 3.8, 6.6, 3.8, 0.014111, 0.000493, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Haryana\\Single\\HaryanaBaseline2.csv";
			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Haryana\\Double\\HaryanaBaseline2.csv";
			ReadWrite demogWriter2 = new ReadWrite(file);
			demogWriter2.openWriteDemogND();

			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Flock\\Haryana\\Triple\\HaryanaBaseline2.csv";
			ReadWrite demogWriter3 = new ReadWrite(file);
			demogWriter3.openWriteDemogND();
			
			file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Population\\Haryana\\Combined\\HaryanaBaseline2.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			
			double localConstantA = 3;
			double transConstantA = 1.35;
			double localConstantB = 2;
			double transConstantB = 1.15;

			for(int k = 0; k < 200; k++){
				Population test = new Population(1, 312, paramsH);
				Flock trojan = new Flock(paramsH, true, 1);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsH, true, 2);
				test.forceTrojan(trojan);
				trojan = new Flock(paramsH, true, 3);
				test.forceTrojan(trojan);

				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
					if(i == 365 * 2) test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
					else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, lTrans/localConstantB, 1 / transConstantB);
					else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
				
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock[] anni = test.getTrojan(3);
				demogWriter1.writeTrojan(k,i,anni[0]);
				demogWriter2.writeTrojan(k,i,anni[1]);
				demogWriter3.writeTrojan(k,i,anni[2]);

			}
				System.out.println("Haryana Season Iteration = "+k);
			}

		}

		public void runPopulationHaryanaVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			//Haryana mortality
			double mortality = 0.85/5;
			double transmission = 3/(4.45*2.5);
				
			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			// ND adjustment - Haryana
			double ndA = 0.7;
			
			double[] paramsH = {1.83, 1.6, 1.02, 0.04*0.9, 0.001 * ndA, 0.0005 * ndA, 3.8, 6.6, 3.8, 0.017 * 0.8, 0.00085 * 0.8, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVMed\\Model_Outputs\\India\\Population\\Haryana_Vacc_Rev_5_3.csv";

			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 100; k++){
				Population test = new Population(5, 312, paramsH);
				Flock trojan = new Flock(paramsH, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
					if(i == 365 * 2) test.setNDVs(0.000001, 0.1, 0.8);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, 0.3, 1.1);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, 0.1, 0.8);
					else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, 0.15, 0.95);
					else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, 0.1, 0.8);
// Define the proportion vaccinated - 0.05 here and the efficacy of the vaccine, then vaccinate eveyr 4 years
					if(i == 365*4)	test.startVaccination(vaccine, 0.05, i, 0.8837, 121);
					if(i >= 365*4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
			}
				System.out.println("Haryana Vaccination Season 5% Iteration = "+k);
			}

		}
		public void runPopulationMPVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(7.96*2.5);
			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double ndA = 0.5;
			double[] paramsMP = {4.21, 2.35, 1.4, 0.05 * 0.675, 0.0015 * ndA, 0.0007 * ndA, 3.1, 7.2, 1.6, 0.019 * 0.7, 0.0009, 0.001};
					
			String file = "C:\\Users\\paul\\Documents\\GALVMed\\Model_Outputs\\India\\Population\\MP_Vacc_Rev_5_3.csv";
			
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 100; k++){
				Population test = new Population(10, 110, paramsMP);
				Flock trojan = new Flock(paramsMP, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
					if(i == 365 * 2) test.setNDVs(0.00001, 0.1175, 0.875);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, 0.24, 1.1);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, 0.1175, 0.875);
					if(i == 365*4)	test.startVaccination(vaccine, 0.05, i, 0.8837,121);
					if(i >= 365*4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
			}
				System.out.println("MP Season 5% Iteration = "+k);
			}

		}
		public void runPopulationOrissaVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			//Orissa mortality
			double mortality = 0.85/5;
			double transmission = 3/(7.15*2.5);

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			// ND adjustment - Orissa
			double ndA = 0.6;
			
			double[] paramsO = {3.8, 2.03, 1.31, 0.03 * 0.6, 0.0015 * ndA, 0.0005 * ndA, 2.9, 5.9, 2.9, 0.019 * 0.65, 0.00085 * 0.65, 0.001};
		
			String file = "C:\\Users\\paul\\Documents\\GALVMed\\Model_Outputs\\India\\Population\\Orissa_Vacc_Rev_5_4.csv";

			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 50; k++){
				Population test = new Population(5, 332, paramsO);
				Flock trojan = new Flock(paramsO, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 10; i++){
					if(i == 365 * 2) test.setNDVs(0.000001, 0.08, 0.8);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, 0.24, 1.1);
					else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, 0.08, 0.8);					
					if(i == 365 * 4) test.startVaccination(vaccine, 0.1, i, 0.8837, 121);
					if(i >= 365 * 4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
			}
				System.out.println("Orissa 10% Vaccination Season Iteration = "+k);
			}
		}
		
//		Programmatic vaccination - X million doses - 
//		here it is implemented by vaccinating a given proportion of flocks - 77% in Haryana
		public void runProgrammeHaryanaVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			//Haryana mortality
			double mortality = 0.85/5;
			double transmission = 3/(4.45*2.5);
				
			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			// ND adjustment - Haryana
			double ndA = 0.7;
			
			double[] paramsH = {1.83, 1.6, 1.02, 0.04*0.9, 0.001 * ndA, 0.0005 * ndA, 3.8, 6.6, 3.8, 0.017 * 0.8, 0.00085 * 0.8, 0.001};
			
			String file = "C:\\Users\\paul\\Documents\\GALVMed\\Model_Outputs\\India\\Programme\\Haryana_Vacc_Prog_77.csv";

			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 200; k++){
				Population test = new Population(5, 312, paramsH);
				Flock trojan = new Flock(paramsH, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 6; i++){
					if(i == 365 * 2) test.setNDVs(0.000001, 0.1, 0.8);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, 0.3, 1.1);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, 0.1, 0.8);
					else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, 0.15, 0.95);
					else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, 0.1, 0.8);
					if(i == 365*4)	test.startVaccination(vaccine, 0.77, i, 0.8837, 121);					
					if(i >= 365*4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
			}
				System.out.println("Haryana Vaccination Programme 77% Iteration = "+k);
			}

		}
		
//		As above, 77% of flocks vaccinated per year 
		public void runProgrammeMPVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(7.96*2.5);
			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double ndA = 0.5;
			double[] paramsMP = {4.21, 2.35, 1.4, 0.05 * 0.675, 0.0015 * ndA, 0.0007 * ndA, 3.1, 7.2, 1.6, 0.019 * 0.7, 0.0009, 0.001};
					
			String file = "C:\\Users\\paul\\Documents\\GALVMed\\Model_Outputs\\India\\Programme\\MP_Vacc_Prog_77.csv";
			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 200; k++){
				Population test = new Population(10, 110, paramsMP);
				Flock trojan = new Flock(paramsMP, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 6; i++){
					if(i == 365 * 2) test.setNDVs(0.00001, 0.1175, 0.875);
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, 0.24, 1.1);
					else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, 0.1175, 0.875);
					if(i == 365*4)	test.startVaccination(vaccine, 0.77, i, 0.8837, 121);
					if(i >= 365*4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
			}
				System.out.println("MP Programme 77% Iteration = "+k);
			}

		}
		public void runProgrammeOrissaVacc() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;
			//Orissa mortality
			double mortality = 0.85/5;
			double transmission = 3/(7.15*2.5);

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			// ND adjustment - Orissa
			double ndA = 0.6;
			
			double[] paramsO = {3.8, 2.03, 1.31, 0.03 * 0.6, 0.0015 * ndA, 0.0005 * ndA, 2.9, 5.9, 2.9, 0.019 * 0.65, 0.00085 * 0.65, 0.001};
		
			String file = "C:\\Users\\paul\\Documents\\GALVMed\\Model_Outputs\\India\\Programme\\Orissa_Vacc_Prog_77.csv";

			ReadWrite writer = new ReadWrite(file);
			writer.openWritemodel();
			for(int k = 0; k < 200; k++){
				Population test = new Population(5, 332, paramsO);
				Flock trojan = new Flock(paramsO, true, 1);
				test.forceTrojan(trojan);
				test.seedND(hpndv);
				test.setNDVs(0.00005);
				for(int i = 0; i < 365 * 6; i++){
					if(i == 365 * 2) test.setNDVs(0.000001, 0.08, 0.8);
				// Tweaked from 0.22 and 1.0
					else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, 0.24, 1.1);
					else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, 0.08, 0.8);					
					if(i == 365 * 4) test.startVaccination(vaccine, 0.77, i, 0.8837, 121);
					if(i >= 365 * 4) test.vaccinate(i);
					
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock anni = test.getTrojan();
				int[] sales = anni.getSaleOutput();
				int[] flockOut = anni.getFlock();
				int[] deaths = anni.getDeathsAge();
				int[] ndDeaths = anni.getndDeathsAge();
				int births = anni.getBirths();
				int[] graduates = anni.getGraduates();
			}
				System.out.println("Orissa 77% Vaccination Programme Iteration = "+k);
			}
		}
		public void runHaryanaInfographicBaseline() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(4.45*2.5);
			transmission = 0.3545;
			double lTrans = 0.328;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsH = {1.83, 1.6, 1.02, 0.032863, 0.000628, 0.000288, 3.8, 6.6, 3.8, 0.014111, 0.000493, 0.001};
			double[] trojanParams = paramsH;
			trojanParams[0] = 1.83 * (10/4.45);
			trojanParams[1] = 1.6 * (10/4.45);
			trojanParams[2] = 1.02 * (10/4.45);
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Infographic\\HaryanaBaselineRev.csv";
//			String file = "C:\\Users\\paul\\Documents\\GALVmed\\Infographic\\HaryanaBaseline.csv";

			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
					
			double localConstantA = 3;
			double transConstantA = 1.35;
			double localConstantB = 2;
			double transConstantB = 1.15;

			for(int k = 0; k < 1000; k++){
				Population test = new Population(1, 312, paramsH);
				Flock trojan = new Flock(trojanParams, true, 1);
				test.forceTrojan(trojan);

				for(int i = 0; i < 365 * 10; i++){
					//test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
					if(i == 91) test.ndTrojan(hpndv);
					if((i - 91) % 365 == 0){
						test.seedND(hpndv);					
						test.setNDVs(0.00005, lTrans, 1.);
					}
					else if((i - 182) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
					else if((i - 273) % 365 == 0) test.setNDVs(0.00002, lTrans/localConstantB, 1 / transConstantB);
					else if((i - 333) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
				
				test.stepPopulation(i);
				Flock[] anni = test.getTrojan(1);
				demogWriter1.writeTrojan(k,i,anni[0]);
			}
				System.out.println("Haryana Season Iteration = "+k);
			}
		}
		public void runHaryanaInfographicVaccine() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(4.45*2.5);
			transmission = 0.3545;
			double lTrans = 0.328;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsH = {1.83, 1.6, 1.02, 0.032863, 0.000628, 0.000288, 3.8, 6.6, 3.8, 0.014111, 0.000493, 0.001};
			double[] trojanParams = paramsH;
			trojanParams[0] = 1.83 * (10/4.45);
			trojanParams[1] = 1.6 * (10/4.45);
			trojanParams[2] = 1.02 * (10/4.45);
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Infographic\\HaryanaVaccineRev.csv";
//			String file = "C:\\Users\\paul\\Documents\\GALVmed\\Infographic\\HaryanaVaccination.csv";

			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
					
			double localConstantA = 3;
			double transConstantA = 1.35;
			double localConstantB = 2;
			double transConstantB = 1.15;

			for(int k = 0; k < 1000; k++){
				Population test = new Population(1, 312, paramsH);
				Flock trojan = new Flock(trojanParams, true, 1);
				test.forceTrojan(trojan);

				for(int i = 0; i < 365 * 10; i++){
					//test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
					if(i == 91) test.ndTrojan(hpndv);
					if((i - 91) % 365 == 0){
						test.seedND(hpndv);					
						test.setNDVs(0.00005, lTrans, 1.);
					}
					else if((i - 182) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
					else if((i - 273) % 365 == 0) test.setNDVs(0.00002, lTrans/localConstantB, 1 / transConstantB);
					else if((i - 333) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);

					if(i == 90){
						test.startVaccinationFixed(vaccine, 0.01, i, 0.8837,92);
						test.startVaccinationTrojans(vaccine, i, 0.8837,1);

					}
					if(i >= 90) test.vaccinate(i);
				
				test.stepPopulation(i);
				Flock[] anni = test.getTrojan(1);
				demogWriter1.writeTrojan(k,i,anni[0]);
			}
				System.out.println("Haryana vaccine Iteration = "+k);
			}
		}
		public void runHaryanaInfographicVaccineIntro() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			// Baseline mortality
			double mortality = 0.85/5;
			double transmission = 3/(4.45*2.5);
			transmission = 0.3545;
			double lTrans = 0.328;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsH = {1.83, 1.6, 1.02, 0.032863, 0.000628, 0.000288, 3.8, 6.6, 3.8, 0.014111, 0.000493, 0.001};
			double[] trojanParams = paramsH;
			trojanParams[0] = 1.83 * (10/4.45);
			trojanParams[1] = 1.6 * (10/4.45);
			trojanParams[2] = 1.02 * (10/4.45);
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Infographic\\HaryanaVaccineIntroRev.csv";
//			String file = "C:\\Users\\paul\\Documents\\GALVmed\\Infographic\\HaryanaVaccination.csv";

			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
					
			double localConstantA = 3;
			double transConstantA = 1.35;
			double localConstantB = 2;
			double transConstantB = 1.15;

			for(int k = 0; k < 1000; k++){
				Population test = new Population(1, 312, paramsH);
				Flock trojan = new Flock(trojanParams, true, 1);
				test.forceTrojan(trojan);

				for(int i = 0; i < 365 * 10; i++){
					//test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
					if((i - 91) % 365 == 0){
						test.seedND(hpndv);					
						test.setNDVs(0.00005, lTrans, 1.);
					}
					else if((i - 182) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
					else if((i - 273) % 365 == 0) test.setNDVs(0.00002, lTrans/localConstantB, 1 / transConstantB);
					else if((i - 333) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);

					if(i == (365 * 7) + 90){
						test.startVaccinationFixed(vaccine, 0.01, i, 0.8837,92);
						test.startVaccinationTrojans(vaccine, i, 0.8837,1);

					}
					if(i >= (365 * 7) + 90) test.vaccinate(i);
				
				test.stepPopulation(i);
				Flock[] anni = test.getTrojan(1);
				demogWriter1.writeTrojan(k,i,anni[0]);
			}
				System.out.println("Haryana vaccine Iteration = "+k);
			}
		}
		public void runHaryanaInfographicBaselineOnOff() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			// Baseline mortality
			double mortality = 0.9/5;
			double transmission = 3/(4.45*2.5);
			transmission = 0.3545;
			double lTrans = 0.328;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsH = {1.83, 1.6, 1.02, 0.032863, 0.000628, 0.000288, 3.8, 6.6, 3.8, 0.014111, 0.000493, 0.001};
			double[] trojanParams = paramsH;
			trojanParams[0] = 1.83 * (10/4.45);
			trojanParams[1] = 1.6 * (10/4.45);
			trojanParams[2] = 1.02 * (10/4.45);
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Infographic\\Artificial\\HaryanaBaseline91_273.csv";

			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
					
			double localConstantA = 3;
			double transConstantA = 1.35;
			double localConstantB = 2;
			double transConstantB = 1.15;

			for(int k = 0; k < 1000; k++){
				Population test = new Population(1, 312, paramsH);
				Flock trojan = new Flock(trojanParams, true, 1);
				test.forceTrojan(trojan);
				test.setElasticity(1.5);

				for(int i = 0; i < 365 * 3; i++){
					//test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
					if((i - 91) % 365 == 0){
						test.seedND(hpndv);	
						test.ndTrojan(2, hpndv);
						test.setNDVs(0.00005, lTrans, 1.);
					}
					else if((i - 122) % 365 == 0) test.setNDVs(0, 0, 0);
//					else if(i % 365 == 0 & i > 0) {
					else if((i - 273) % 365 == 0) {

						//test.seedND(hpndv);	
						test.ndTrojan(2, hpndv);
						test.setNDVs(0.00005, lTrans, 1.);
					}
					else if((i - 304) % 365 == 0) test.setNDVs(0, 0, 0);
				
				test.stepPopulation(i);
				Flock[] anni = test.getTrojan(1);
				demogWriter1.writeTrojan(k,i,anni[0]);
	//			System.out.println(i);

			}
				System.out.println("Haryana Season Iteration = "+k);
			}
		}
		public void runHaryanaInfographicVaccineOnOff() throws Exception{

			String NDPath = "High path";
			int latent = 5;
			int infectious = 5;
			int prot = 105;
			int titre = 300;
			int maternalA = 42;

			// Baseline mortality
			double mortality = 0.9/5;
			double transmission = 3/(4.45*2.5);
			transmission = 0.3545;
			double lTrans = 0.328;

			NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
			NDInfection vaccine = new NDInfection(105);

			double[] paramsH = {1.83, 1.6, 1.02, 0.032863, 0.000628, 0.000288, 3.8, 6.6, 3.8, 0.014111, 0.000493, 0.001};
			double[] trojanParams = paramsH;
			trojanParams[0] = 1.83 * (10/4.45);
			trojanParams[1] = 1.6 * (10/4.45);
			trojanParams[2] = 1.02 * (10/4.45);
			
			String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\India\\Infographic\\Artificial\\HaryanaVaccine91_273.csv";

			ReadWrite demogWriter1 = new ReadWrite(file);
			demogWriter1.openWriteDemogND();
					
			double localConstantA = 3;
			double transConstantA = 1.35;
			double localConstantB = 2;
			double transConstantB = 1.15;

			for(int k = 0; k < 1000; k++){
				Population test = new Population(1, 312, paramsH);
				Flock trojan = new Flock(trojanParams, true, 1);
				test.forceTrojan(trojan);
				test.setElasticity(1.5);

				for(int i = 0; i < 365 * 3; i++){
					//test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
					if((i - 182) % 365 == 0){
						test.seedND(hpndv);	
						test.ndTrojan(2, hpndv);
						test.setNDVs(0.00005, lTrans, 1.);
					}
					else if((i - 213) % 365 == 0) test.setNDVs(0, 0, 0);
					else if((i - 273) % 365 == 0) {

						//test.seedND(hpndv);	
						test.ndTrojan(2, hpndv);
						test.setNDVs(0.00005, lTrans, 1.);
					}
					else if((i - 304) % 365 == 0) test.setNDVs(0, 0, 0);
					if(i == 89){
						test.startVaccinationFixed(vaccine, 1, i, 0.8837,91);
						test.startVaccinationTrojans(vaccine, i, 0.8837,1);

					}
					if(i >= 89) test.vaccinate(i);
			
				test.stepPopulation(i);
				Flock[] anni = test.getTrojan(1);
				demogWriter1.writeTrojan(k,i,anni[0]);
	//			System.out.println(i);
			}
				System.out.println("Haryana Vaccine Iteration = "+k);
			}
		}

}
