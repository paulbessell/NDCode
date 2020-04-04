/*********************************
 * 
 * driver.java contains methods for implementing the Newcastle Disease model
 * 
 * 
 ********************************/


import java.util.*;

import org.apache.commons.math3.distribution.*;

public class driverAfrica {

	public static void main(String[] args) throws Exception{
// 		TODO Auto-generated method stub
		
//		Call and run the required method 
		driverAfrica myDriver = new driverAfrica();			
//		myDriver.runSingleFlockDemogAfrica();
//		myDriver.runPopulationEthiopiaBigBaseline();
//		myDriver.runPopulationEthiopiaBigTrojanVacc();
		myDriver.runPopulationEthiopiaBigTrojanVacc4PY();
//		myDriver.runPopulationKenyaBigBaseline();
//		myDriver.runPopulationBurkinaBigBaseline();
		
	
		myDriver.runPopulationBurkinaBigTrojanVacc();
//		myDriver.runPopulationBurkinaBigTrojanVacc4PY();
/*		myDriver.runPopulationKenyaBigTrojanVacc();
		myDriver.runPopulationKenyaBigTrojanVacc4PY();*/
	}
	
	public driverAfrica(){}


//		 Driver methods for three states in India - just the flock demographics
	public void runSingleFlockDemogAfrica() throws Exception{
		String file = "Outputs\\Africa\\Tanzania\\TestDemog.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWriteDemog();
		
//		This defines parameters for each state based on values fitted using the code below
//		double[] paramsBF = {26.4, 5.5, 1.6, 0.04, 0.001375, 0.001, 3.5, 7.6, 2, 0.023, 0.001, 0.001};
//		double[] paramsEthiopia = {4.57, 2.44, 0.61, 0.04, 0.001375, 0.001, 3., 6.7, 4.5, 0.018, 0.001, 0.001};
		double[] paramsTanzania = {12.95, 4.45, 1.51, 0.035, 0.001375, 0.001, 2.5, 7.81, 2.86, 0.017, 0.001, 0.001};
		
		for(int h = 1; h < 100; h++){
		Flock test = new Flock(paramsTanzania, false, 1);

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
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Ethiopia\\Single\\Ethiopia_BaselineFinalFinal2.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Ethiopia\\Double\\Ethiopia_BaselineFinalFinal2.csv";
		ReadWrite demogWriter2 = new ReadWrite(file);
		demogWriter2.openWriteDemogND();

		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Ethiopia\\Triple\\Ethiopia_BaselineFinalFinal2.csv";
		ReadWrite demogWriter3 = new ReadWrite(file);
		demogWriter3.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Population\\Ethiopia\\Combined\\Ethiopia_BaselineFinalFinal2.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
		
		double introConstant = 10;
		double localConstant = 2.;
		double transConstant = 1.35;

		for(int k = 0; k < 400; k++){
			Population test = new Population(1, 250, paramsE);
			Flock trojan = new Flock(paramsE, true, 1);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsE, true, 2);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsE, true, 3);
			test.forceTrojan(trojan);

			test.seedND(hpndv);
			test.setNDVs(0.00005, lTrans, 1);
			for(int i = 0; i < 365 * 10; i++){

				if(i == 365 * 2) test.setNDVs(0.000005, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00005, lTrans, 1);
				else if(i > (365 * 2) && (i - 242) % 365 == 0) test.setNDVs(0.000005, lTrans / localConstant, 1 / transConstant);					
				
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock[] anni = test.getTrojan(3);
				demogWriter1.writeTrojan(k,i,anni[0]);
				demogWriter2.writeTrojan(k,i,anni[1]);
				demogWriter3.writeTrojan(k,i,anni[2]);
			}
			System.out.println("Ethiopia Season Iteration = "+k);
		}

	}
	public void runPopulationEthiopiaBigTrojanVacc() throws Exception{

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
		NDInfection vaccine = new NDInfection(105);

		double[] paramsE = {4.57, 2.44, 0.61, 0.0329, 0.00214, 0.0003277, 2.5, 6.7, 4.5, 0.0159, 0.001126, 0.001}; // Baseline 4
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Ethiopia\\Single\\Ethiopia_Vacc10EP10.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Ethiopia\\Double\\Ethiopia_Vacc10EP10.csv";
		ReadWrite demogWriter2 = new ReadWrite(file);
		demogWriter2.openWriteDemogND();

		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Ethiopia\\Triple\\Ethiopia_Vacc10EP10.csv";
		ReadWrite demogWriter3 = new ReadWrite(file);
		demogWriter3.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Population\\Ethiopia\\Combined\\Ethiopia_Vacc10EP10.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
		
		double introConstant = 10;
		double localConstant = 2.;
		double transConstant = 1.35;

		for(int k = 0; k < 200; k++){
			Population test = new Population(1, 250, paramsE);
			Flock trojan = new Flock(paramsE, true, 1);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsE, true, 2);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsE, true, 3);
			test.forceTrojan(trojan);

			test.seedND(hpndv);
			test.setNDVs(0.00005, lTrans, 1);
			for(int i = 0; i < 365 * 10; i++){

				if(i == 365 * 2) test.setNDVs(0.000005, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00005, lTrans, 1);
				else if(i > (365 * 2) && (i - 242) % 365 == 0) test.setNDVs(0.000005, lTrans / localConstant, 1 / transConstant);					
				if(i == 365*4){
//	Define a vaccination of 10% of flocks with an efficacy of 0.8837
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
			System.out.println("Ethiopia Season Iteration = "+k);
		}

	}
	public void runPopulationEthiopiaBigTrojanVacc4PY() throws Exception{

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
		NDInfection vaccine = new NDInfection(105);

		double[] paramsE = {4.57, 2.44, 0.61, 0.0329, 0.00214, 0.0003277, 2.5, 6.7, 4.5, 0.0159, 0.001126, 0.001}; // Baseline 4
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Ethiopia\\Single\\Ethiopia_Vacc10EP10_4PY.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Ethiopia\\Double\\Ethiopia_Vacc10EP10_4PY.csv";
		ReadWrite demogWriter2 = new ReadWrite(file);
		demogWriter2.openWriteDemogND();

		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Ethiopia\\Triple\\Ethiopia_Vacc10EP10_4PY.csv";
		ReadWrite demogWriter3 = new ReadWrite(file);
		demogWriter3.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Population\\Ethiopia\\Combined\\Ethiopia_Vacc10EP10_4PY.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
		
		double introConstant = 10;
		double localConstant = 2.;
		double transConstant = 1.35;

		for(int k = 0; k < 200; k++){
			Population test = new Population(1, 250, paramsE);
			Flock trojan = new Flock(paramsE, true, 1);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsE, true, 2);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsE, true, 3);
			test.forceTrojan(trojan);

			test.seedND(hpndv);
			test.setNDVs(0.00005, lTrans, 1);
			for(int i = 0; i < 365 * 10; i++){

				if(i == 365 * 2) test.setNDVs(0.000005, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00005, lTrans, 1);
				else if(i > (365 * 2) && (i - 242) % 365 == 0) test.setNDVs(0.000005, lTrans / localConstant, 1 / transConstant);					
				if(i == 365*4){
//	Define a vaccination of 10% of flocks with an efficacy of 0.8837
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
			}
			System.out.println("Ethiopia Season Iteration = "+k);
		}

	}
	public void runPopulationBurkinaBigBaseline() throws Exception{

		String NDPath = "High path";
		int latent = 5;
		int infectious = 5;
		int prot = 105;
		int titre = 300;
		int maternalA = 42;
		// Baseline mortality
		double mortality = 0.85/5;
		double transmission = 3/(7.16*2.5);
		transmission = 0.03739;
		double lTrans = 0.4336;
		

		NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);

		double[] paramsB = {26.4, 6.2, 1.8, 0.0112, 0.00111, 0.000437, 2.5, 7.6, 4.2, 0.0194, 0.0015, 0.003}; // Baseline 2
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Burkina\\Single\\Burkina_BaselineTest.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Burkina\\Double\\Burkina_BaselineTest.csv";
		ReadWrite demogWriter2 = new ReadWrite(file);
		demogWriter2.openWriteDemogND();

		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Burkina\\Triple\\Burkina_BaselineTest.csv";
		ReadWrite demogWriter3 = new ReadWrite(file);
		demogWriter3.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Population\\Burkina\\Combined\\Burkina_BaselineTest.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
		
		double introConstant = 10;
		double localConstant = 2.6;
		double transConstant = 2.6;

		for(int k = 0; k < 400; k++){
			Population test = new Population(1, 200, paramsB);
			Flock trojan = new Flock(paramsB, true, 1);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsB, true, 2);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsB, true, 3);
			test.forceTrojan(trojan);

			test.setHenCockRatio(3.4);
			test.setMaxChickAge(56);

			test.seedND(hpndv);
			test.setNDVs(0.00005, lTrans, 1);
			for(int i = 0; i < 365 * 10; i++){

				if(i == 365 * 1) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, lTrans, 1);
				else if(i > (365 * 2) && (i - 270) % 365 == 0) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);					
				
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock[] anni = test.getTrojan(3);
				demogWriter1.writeTrojan(k,i,anni[0]);
				demogWriter2.writeTrojan(k,i,anni[1]);
				demogWriter3.writeTrojan(k,i,anni[2]);
			}
			System.out.println("Burkina Season Iteration = "+k);
		}

	}
	public void runPopulationBurkinaBigTrojanVacc() throws Exception{

		String NDPath = "High path";
		int latent = 5;
		int infectious = 5;
		int prot = 105;
		int titre = 300;
		int maternalA = 42;
		// Baseline mortality
		double mortality = 0.85/5;
		double transmission = 3/(7.16*2.5);
		transmission = 0.03739;
		double lTrans = 0.4336;
		

		NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
		NDInfection vaccine = new NDInfection(105);

		double[] paramsB = {26.4, 6.2, 1.8, 0.0112, 0.00111, 0.000437, 2.5, 7.6, 4.2, 0.0194, 0.0015, 0.003}; // Baseline 2
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Burkina\\Single\\Burkina_Vacc10EP10.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Burkina\\Double\\Burkina_Vacc10EP10.csv";
		ReadWrite demogWriter2 = new ReadWrite(file);
		demogWriter2.openWriteDemogND();

		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Burkina\\Triple\\Burkina_Vacc10EP10.csv";
		ReadWrite demogWriter3 = new ReadWrite(file);
		demogWriter3.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Population\\Burkina\\Combined\\Burkina_Vacc10EP10.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
		
		double introConstant = 10;
		double localConstant = 2.6;
		double transConstant = 2.6;

		for(int k = 0; k < 200; k++){
			Population test = new Population(1, 200, paramsB);
			Flock trojan = new Flock(paramsB, true, 1);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsB, true, 2);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsB, true, 3);
			test.forceTrojan(trojan);

			test.setHenCockRatio(3.4);
			test.setMaxChickAge(56);

			test.seedND(hpndv);
			test.setNDVs(0.00005, lTrans, 1);
			for(int i = 0; i < 365 * 10; i++){

				if(i == 365 * 1) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, lTrans, 1);
				else if(i > (365 * 2) && (i - 270) % 365 == 0) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);					
				if(i == 365*4){
//	Define a vaccination of 10% of flocks with an efficacy of 0.8837
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
			}
			System.out.println("Burkina Season Iteration = "+k);
		}
	}
	
	public void runPopulationBurkinaBigTrojanVacc4PY() throws Exception{

		String NDPath = "High path";
		int latent = 5;
		int infectious = 5;
		int prot = 105;
		int titre = 300;
		int maternalA = 42;
		// Baseline mortality
		double mortality = 0.85/5;
		double transmission = 3/(7.16*2.5);
		transmission = 0.03739;
		double lTrans = 0.4336;
		

		NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
		NDInfection vaccine = new NDInfection(105);

		double[] paramsB = {26.4, 6.2, 1.8, 0.0112, 0.00111, 0.000437, 2.5, 7.6, 4.2, 0.0194, 0.0015, 0.003}; // Baseline 2
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Burkina\\Single\\Burkina_Vacc10EP10_4PY.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Burkina\\Double\\Burkina_Vacc10EP10_4PY.csv";
		ReadWrite demogWriter2 = new ReadWrite(file);
		demogWriter2.openWriteDemogND();

		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Burkina\\Triple\\Burkina_Vacc10EP10_4PY.csv";
		ReadWrite demogWriter3 = new ReadWrite(file);
		demogWriter3.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Population\\Burkina\\Combined\\Burkina_Vacc10EP10_4PY.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
		
		double introConstant = 10;
		double localConstant = 2.6;
		double transConstant = 2.6;

		for(int k = 0; k < 200; k++){
			Population test = new Population(1, 200, paramsB);
			Flock trojan = new Flock(paramsB, true, 1);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsB, true, 2);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsB, true, 3);
			test.forceTrojan(trojan);

			test.setHenCockRatio(3.4);
			test.setMaxChickAge(56);

			test.seedND(hpndv);
			test.setNDVs(0.00005, lTrans, 1);
			for(int i = 0; i < 365 * 10; i++){

				if(i == 365 * 1) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, lTrans, 1);
				else if(i > (365 * 2) && (i - 270) % 365 == 0) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);					
				if(i == 365*4){
//	Define a vaccination of 10% of flocks with an efficacy of 0.8837
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
			}
			System.out.println("Burkina Season Iteration = "+k);
		}

	}
	public void runPopulationKenyaBigBaseline() throws Exception{

		String NDPath = "High path";
		int latent = 5;
		int infectious = 5;
		int prot = 105;
		int titre = 300;
		int maternalA = 42;
		// Baseline mortality
		double mortality = 0.85/5;
		double transmission = 3/(7.16*2.5);
		transmission = 0.053;
		double lTrans = 0.189;
		

		NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);

		double[] paramsK = {12.95, 4.45, 1.51, 0.0183, 0.000769, 0.000467, 2.5, 7.8, 2.86, 0.0115, 0.000733, 0.003}; // Baseline 2
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Tanzania\\Single\\Kenya_Baseline.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Tanzania\\Double\\Kenya_Baseline.csv";
		ReadWrite demogWriter2 = new ReadWrite(file);
		demogWriter2.openWriteDemogND();

		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Tanzania\\Triple\\Kenya_Baseline.csv";
		ReadWrite demogWriter3 = new ReadWrite(file);
		demogWriter3.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Population\\Tanzania\\Combined\\Kenya_Baseline.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
		
		double introConstant = 10;
		double localConstant = 2.6;
		double transConstant = 1.35;

		for(int k = 0; k < 400; k++){
			Population test = new Population(1, 200, paramsK);
			Flock trojan = new Flock(paramsK, true, 1);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsK, true, 2);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsK, true, 3);
			test.forceTrojan(trojan);

			test.setHenCockRatio(3.);
			test.setMaxChickAge(56);
			test.setMaxGrowerAge(180);

			test.seedND(hpndv);
			test.setNDVs(0.00005);
			for(int i = 0; i < 365 * 10; i++){

				if(i == 365 * 1) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, lTrans, 1);
				else if(i > (365 * 2) && (i - 270) % 365 == 0) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);					
				
				test.stepPopulation(i);
				writer.writemodel(k, i, test.getComps());
				Flock[] anni = test.getTrojan(3);
				demogWriter1.writeTrojan(k,i,anni[0]);
				demogWriter2.writeTrojan(k,i,anni[1]);
				demogWriter3.writeTrojan(k,i,anni[2]);
			}
			System.out.println("Kenya Season Iteration = "+k);
		}
	}	
	public void runPopulationKenyaBigTrojanVacc() throws Exception{

		String NDPath = "High path";
		int latent = 5;
		int infectious = 5;
		int prot = 105;
		int titre = 300;
		int maternalA = 42;
		// Baseline mortality
		double mortality = 0.85/5;
		double transmission = 3/(7.16*2.5);
		transmission = 0.053;
		double lTrans = 0.189;
		

		NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
		NDInfection vaccine = new NDInfection(105);

		double[] paramsK = {12.95, 4.45, 1.51, 0.0183, 0.000769, 0.000467, 2.5, 7.8, 2.86, 0.0115, 0.000733, 0.003}; // Baseline 2
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Tanzania\\Single\\Kenya_Vacc10EP10_2.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Tanzania\\Double\\Kenya_Vacc10EP10_2.csv";
		ReadWrite demogWriter2 = new ReadWrite(file);
		demogWriter2.openWriteDemogND();

		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Tanzania\\Triple\\Kenya_Vacc10EP10_2.csv";
		ReadWrite demogWriter3 = new ReadWrite(file);
		demogWriter3.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Population\\Tanzania\\Combined\\Kenya_Vacc10EP10_2.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
		
		double introConstant = 10;
		double localConstant = 2.6;
		double transConstant = 1.35;

		for(int k = 0; k < 200; k++){
			Population test = new Population(1, 200, paramsK);
			Flock trojan = new Flock(paramsK, true, 1);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsK, true, 2);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsK, true, 3);
			test.forceTrojan(trojan);

			test.setHenCockRatio(3.);
			test.setMaxChickAge(56);
			test.setMaxGrowerAge(180);

			test.seedND(hpndv);
			test.setNDVs(0.00005);
			for(int i = 0; i < 365 * 10; i++){

				if(i == 365 * 1) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, lTrans, 1);
				else if(i > (365 * 2) && (i - 270) % 365 == 0) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);					
				if(i == 365*4){
//	Define a vaccination of 10% of flocks with an efficacy of 0.8837
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
			}
			System.out.println("Kenya Season Iteration = "+k);
		}
	}
	public void runPopulationKenyaBigTrojanVacc4PY() throws Exception{

		String NDPath = "High path";
		int latent = 5;
		int infectious = 5;
		int prot = 105;
		int titre = 300;
		int maternalA = 42;
		// Baseline mortality
		double mortality = 0.85/5;
		double transmission = 3/(7.16*2.5);
		transmission = 0.053;
		double lTrans = 0.189;
		

		NDInfection hpndv = new NDInfection(NDPath, latent, infectious, prot, titre, maternalA, mortality, transmission);
		NDInfection vaccine = new NDInfection(105);

		double[] paramsK = {12.95, 4.45, 1.51, 0.0183, 0.000769, 0.000467, 2.5, 7.8, 2.86, 0.0115, 0.000733, 0.003}; // Baseline 2
		
		String file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Tanzania\\Single\\Kenya_Vacc10EP104PY_2.csv";
		ReadWrite demogWriter1 = new ReadWrite(file);
		demogWriter1.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Tanzania\\Double\\Kenya_Vacc10EP104PY_2.csv";
		ReadWrite demogWriter2 = new ReadWrite(file);
		demogWriter2.openWriteDemogND();

		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Flock\\Tanzania\\Triple\\Kenya_Vacc10EP104PY_2.csv";
		ReadWrite demogWriter3 = new ReadWrite(file);
		demogWriter3.openWriteDemogND();
		
		file = "C:\\Users\\paul\\Documents\\GALVmed\\NewcastleDisease\\Model\\Africa\\Population\\Tanzania\\Combined\\Kenya_Vacc10EP104PY_2.csv";
		ReadWrite writer = new ReadWrite(file);
		writer.openWritemodel();
		
		double introConstant = 10;
		double localConstant = 2.6;
		double transConstant = 1.35;

		for(int k = 0; k < 200; k++){
			Population test = new Population(1, 200, paramsK);
			Flock trojan = new Flock(paramsK, true, 1);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsK, true, 2);
			test.forceTrojan(trojan);
			trojan = new Flock(paramsK, true, 3);
			test.forceTrojan(trojan);

			test.setHenCockRatio(3.);
			test.setMaxChickAge(56);
			test.setMaxGrowerAge(180);

			test.seedND(hpndv);
			test.setNDVs(0.00005);
			for(int i = 0; i < 365 * 10; i++){

				if(i == 365 * 1) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);
				else if(i > (365 * 2) && (i - 121) % 365 == 0) test.setNDVs(0.00001, lTrans, 1);
				else if(i > (365 * 2) && (i - 270) % 365 == 0) test.setNDVs(0.000001, lTrans / localConstant, 1 / transConstant);					
				if(i == 365*4){
//	Define a vaccination of 10% of flocks with an efficacy of 0.8837
					test.startVaccination(vaccine, 0.1, i, 0.8837, 92);
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
			}
			System.out.println("Kenya Season Iteration = "+k);
		}
	}
}
