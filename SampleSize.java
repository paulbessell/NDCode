import java.util.*;

import org.apache.commons.math3.distribution.BinomialDistribution;

public class SampleSize {
	public static void main(String[] args) throws Exception{
// 		TODO Auto-generated method stub
		
//		Call and run the required method 
//		System.out.println(new BinomialDistribution(1, 0.2).sample());
		SampleSize myDriver = new SampleSize();
/*		myDriver.runPopulationOrissaBigBaseline();
		myDriver.runPopulationHaryanaBigTrojanVacc4Y();
		myDriver.runPopulationMPBigBaseline();
*/
	/*	
		myDriver.runPopulationHaryanaBigBaseline();
		myDriver.runPopulationEthiopiaBigBaseline();
		myDriver.runPopulationMPBigTrojanVacc4Y();
*/
		myDriver.runPopulationOrissaBigTrojanVacc4Y();
		myDriver.runPopulationEthiopiaVacc4PY();
		
	}
	public SampleSize(){}
	
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
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\SampleSizes\\Orissa_Baseline_Sales.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteSampleSize();
			
		double introConstant = 10;
		double localConstant = 2.6;
		double transConstant = 1.35;
		int iFlocks = 12;
		for(int k = 0; k < 1200; k++){
			Population test = new Population(1, 332, paramsO);
			test.setElasticity(2);
			for(int h = 0; h < iFlocks; h++){
				Flock trojan = new Flock(paramsO, false, 1);
				test.forceTrojan(trojan);
			}
			
			test.seedND(hpndv);
			test.setNDVs(0.00005);
			for(int i = 0; i < 365 * 6; i++){

				if(i == 365 * 2) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, lTrans, 1);
				else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);					
				
				test.stepPopulation(i);
				Flock[] anni = test.getTrojan(iFlocks);
				if((i-1368-1) % 92 ==0) {
					demogWriter1.writeTrojan(k,i,anni);
					for(int curr = 0; curr < anni.length; curr++) anni[curr].resetCumSales();
				}
			}
			System.out.println("Orissa Season Iteration = "+k);
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
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\SampleSizes\\MP_Baseline_Sales.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteSampleSize();
		int iFlocks = 12;
		for(int k = 0; k < 1200; k++){
			Population test = new Population(1, 110, paramsMP);
			test.setElasticity(2);
			for(int h = 0; h < iFlocks; h++){
				Flock trojan = new Flock(paramsMP, false, 1);
				test.forceTrojan(trojan);
			}

			test.seedND(hpndv);
			test.setNDVs(0.00005);
			for(int i = 0; i < 365 * 6; i++){

				if(i == 365 * 2) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
				else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
				else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
				
				test.stepPopulation(i);
				Flock[] anni = test.getTrojan(iFlocks);
				if((i-1368-1) % 92 ==0) {
					demogWriter1.writeTrojan(k,i,anni);
					for(int curr = 0; curr < anni.length; curr++) anni[curr].resetCumSales();
				}
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
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\SampleSizes\\HaryanaBaseline_Sales.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteSampleSize();
		
		
		double localConstantA = 3;
		double transConstantA = 1.35;
		double localConstantB = 2;
		double transConstantB = 1.15;
		int iFlocks = 12;
		for(int k = 0; k < 1200; k++){
			Population test = new Population(1, 312, paramsH);
			test.setElasticity(2);
			for(int h = 0; h < iFlocks; h++){
				Flock trojan = new Flock(paramsH, false, 1);
				test.forceTrojan(trojan);
			}

			test.seedND(hpndv);
			test.setNDVs(0.00005);
			for(int i = 0; i < 365 * 6; i++){
				if(i == 365 * 2) test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
				else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
				else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
				else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, lTrans/localConstantB, 1 / transConstantB);
				else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
			
			test.stepPopulation(i);
			Flock[] anni = test.getTrojan(iFlocks);
			if((i-1368-1) % 92 ==0) {
				demogWriter1.writeTrojan(k,i,anni);
				for(int curr = 0; curr < anni.length; curr++) anni[curr].resetCumSales();
			}

		}
			System.out.println("Haryana Season Iteration = "+k);
		}

	}
	public void runPopulationEthiopiaBigBaseline() throws Exception{

		String NDPath = "High path";
		int latent = 5;
		int infectious = 5;
		int prot = 105;
		int titre = 300;
		int maternalA = 42;
		// Baseline mortality
		double mortality = 0.85/5;
		double transmission = 3/(7.16*2.5);
		transmission = 0.193;
		double lTrans = 0.1102;
		

		NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
		double chicks = 2.84;
		double growers = 1.73;
		double hens = 2.44;
		double cocks = 0.61;
		double clutches = 2.5;
		double hatchRate = 6.7;
		double eggOffTake = 4.5;

		double[] paramsE = {4.57, 2.44, 0.61, 0.0329, 0.00214, 0.0003277, 2.5, 6.7, 4.5, 0.0159, 0.001126, 0.001}; // Baseline 4
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\SampleSizes\\Ethiopia_Baseline_Sales.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteSampleSize();
			
		double introConstant = 10;
		double localConstant = 2.6;
		double transConstant = 1.35;
		int iFlocks = 12;
		for(int k = 0; k < 1200; k++){
			Population test = new Population(1, 200, paramsE);
			test.setElasticity(2);
			for(int h = 0; h < iFlocks; h++){
				Flock trojan = new Flock(paramsE, false, 1);
				test.forceTrojan(trojan);
			}
			
			test.seedND(hpndv);
			test.setNDVs(0.00005);
			for(int i = 0; i < 365 * 6; i++){

				if(i == 365 * 2) test.setNDVs(0.000005, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00005, lTrans, 1);
				else if(i > (365 * 2) && (i - 242) % 365 == 0) test.setNDVs(0.000005, lTrans / localConstant, 1 / transConstant);					
				
				test.stepPopulation(i);
				Flock[] anni = test.getTrojan(iFlocks);
				if((i-1368-1) % 92 ==0) {
					demogWriter1.writeTrojan(k,i,anni);
					for(int curr = 0; curr < anni.length; curr++) anni[curr].resetCumSales();
				}
			}
			System.out.println("Ethiopia Season Iteration = "+k);
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
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\SampleSizes\\OrissaVacc104PY_Sales.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteSampleSize();
		
		
		double introConstant = 10;
		double localConstant = 2.6;
		double transConstant = 1.35;
		int iFlocks = 12;
		for(int k = 0; k < 2400; k++){
			Population test = new Population(1, 332, paramsO);
			test.setElasticity(2);
			for(int h = 0; h < iFlocks; h++){
				Flock trojan = new Flock(paramsO, false, 1);
				test.forceTrojan(trojan);
			}
			

			test.seedND(hpndv);
			test.setNDVs(0.00005);
			for(int i = 0; i < 365 * 6; i++){

				if(i == 365 * 2) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00001, lTrans, 1);
				else if(i > (365 * 2) && (i - 364) % 365 == 0) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);					

				if(i == 365*4){
//	Define a vaccination of 10% of flocks with an efficacy of 0.8837
					test.startVaccinationFixed(vaccine, 0.5, i, 0.8837,92);
//					test.setProdImprovement(1.2);
//					test.setElasticity(2);
				}
				if(i >= 365*4) test.vaccinate(i);
			
				test.stepPopulation(i);
				Flock[] anni = test.getTrojan(iFlocks);
				if((i-1368-1) % 92 ==0) {
					demogWriter1.writeTrojan(k,i,anni);
					for(int curr = 0; curr < anni.length; curr++) anni[curr].resetCumSales();
				}
			}
			System.out.println("Orissa Season Iteration = "+k);
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
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\SampleSizes\\MPVacc10EP204PY_Sales.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteSampleSize();
		
		int iFlocks = 12;
		for(int k = 0; k < 2400; k++){
			Population test = new Population(1, 110, paramsMP);
			test.setElasticity(2);
			for(int h = 0; h < iFlocks; h++){
				Flock trojan = new Flock(paramsMP, false, 1);
				test.forceTrojan(trojan);
			}

			test.seedND(hpndv);
			test.setNDVs(0.00005);
			for(int i = 0; i < 365 * 6; i++){
				if(i == 365 * 2) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
				else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
				else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.00001, lTrans * 0.5, 0.8);
				if(i == 365*4){
					test.startVaccinationFixed(vaccine, 0.5, i, 0.8837, 92);

				}
				if(i >= 365*4) test.vaccinate(i);
				
			test.stepPopulation(i);
			Flock[] anni = test.getTrojan(iFlocks);
			if((i-1368-1) % 92 ==0) {
				demogWriter1.writeTrojan(k,i,anni);
				for(int curr = 0; curr < anni.length; curr++) anni[curr].resetCumSales();
			}
		}
			System.out.println("MP Season Iteration = "+k);
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
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\SampleSizes\\HaryanaVacc10EP104PY_Sales.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteSampleSize();
		
		double localConstantA = 3;
		double transConstantA = 1.35;
		double localConstantB = 2;
		double transConstantB = 1.15;
		
		int iFlocks = 12;
		for(int k = 0; k < 2400; k++){
			Population test = new Population(1, 312, paramsH);
			test.setElasticity(2);
			for(int h = 0; h < iFlocks; h++){
				Flock trojan = new Flock(paramsH, false, 1);
				test.forceTrojan(trojan);
			}

			test.seedND(hpndv);
			test.setNDVs(0.00005);
			for(int i = 0; i < 365 * 6; i++){
				if(i == 365 * 2) test.setNDVs(0.000001, lTrans/localConstantA, 1 / transConstantA);
				else if(i > (365 * 2) && (i - 91) % 365 == 0) test.setNDVs(0.00005, lTrans, 1.);
				else if(i > (365 * 2) && (i - 182) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);
				else if(i > (365 * 2) && (i - 273) % 365 == 0) test.setNDVs(0.00002, lTrans/localConstantB, 1 / transConstantB);
				else if(i > (365 * 2) && (i - 333) % 365 == 0) test.setNDVs(0.000005, lTrans/localConstantA, 1 / transConstantA);

				if(i == 365*4){
//	Define a vaccination of 10% of flocks with an efficacy of 0.8837
//					test.startVaccination(vaccine, 0.1, i, 0.8837,121);
					test.startVaccinationFixed(vaccine, 0.5, i, 0.8837,92);
				}
				if(i >= 365*4) test.vaccinate(i);
				
			test.stepPopulation(i);
			Flock[] anni = test.getTrojan(iFlocks);
			if((i-1368-1) % 92 ==0) {
				demogWriter1.writeTrojan(k,i,anni);
				for(int curr = 0; curr < anni.length; curr++) anni[curr].resetCumSales();
			}

		}
			System.out.println("Haryana Season Iteration = "+k);
		}

	}

	
	public void runPopulationEthiopiaVacc4PY() throws Exception{

		String NDPath = "High path";
		int latent = 5;
		int infectious = 5;
		int prot = 105;
		int titre = 300;
		int maternalA = 42;
		// Baseline mortality
		double mortality = 0.85/5;
		double transmission = 3/(7.16*2.5);
		transmission = 0.193;
		double lTrans = 0.1102;
		

		NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
		NDInfection vaccine = new NDInfection(105);

		double chicks = 2.84;
		double growers = 1.73;
		double hens = 2.44;
		double cocks = 0.61;
		double clutches = 2.5;
		double hatchRate = 6.7;
		double eggOffTake = 4.5;

		double[] paramsE = {4.57, 2.44, 0.61, 0.0329, 0.00214, 0.0003277, 2.5, 6.7, 4.5, 0.0159, 0.001126, 0.001}; // Baseline 4
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\SampleSizes\\Ethiopia_Vacc4PY_Sales.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteSampleSize();
			
		double introConstant = 10;
		double localConstant = 2.6;
		double transConstant = 1.35;
		int iFlocks = 12;
		for(int k = 0; k < 2400; k++){
			Population test = new Population(1, 200, paramsE);
			test.setElasticity(2);
			for(int h = 0; h < iFlocks; h++){
				Flock trojan = new Flock(paramsE, false, 1);
				test.forceTrojan(trojan);
			}
			
			test.seedND(hpndv);
			test.setNDVs(0.00005);
			for(int i = 0; i < 365 * 6; i++){

				if(i == 365 * 2) test.setNDVs(0.000005, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00005, lTrans, 1);
				else if(i > (365 * 2) && (i - 242) % 365 == 0) test.setNDVs(0.000005, lTrans / localConstant, 1 / transConstant);					
				if(i == 365*4){
//	Define a vaccination of 10% of flocks with an efficacy of 0.8837
//					test.startVaccination(vaccine, 0.1, i, 0.8837,121);
					test.startVaccinationFixed(vaccine, 0.5, i, 0.8837,92);
				}
				if(i >= 365*4) test.vaccinate(i);
				
				test.stepPopulation(i);
				Flock[] anni = test.getTrojan(iFlocks);
				if((i-1368-1) % 92 ==0) {
					demogWriter1.writeTrojan(k,i,anni);
					for(int curr = 0; curr < anni.length; curr++) anni[curr].resetCumSales();
				}
			}
			System.out.println("Ethiopia vaccination Iteration = "+k);
		}

	}

}
