/**********************************
 * 
 * Class for writing outputs as csv files 
 * 
 ********************************/

import java.io.*;
import java.util.*;


public class ReadWrite {
	public FileWriter fw;
	public BufferedWriter bw;

	public ReadWrite(String outFile) throws Exception{
		this.fw = new FileWriter(outFile);
		this.bw = new BufferedWriter(this.fw);
	}
	public static String newline = System.getProperty("line.separator");

	public void openWriteABC(String output) throws Exception {
		
		this.bw.write(output + newline);
		this.bw.flush();
	}

	public void openWritemodel() throws Exception {
		
		this.bw.write("iter,day,S,E,I,P,R,D,Dc,nFInf,N,OTg,OTa,BI,Cl,Egg,DV,OVg,OVa,BV,CV,EV,FV,nChick,nGrower,nHen,nCock,NV,NF,V" + newline);
		this.bw.flush();
	}
	
	public void writemodel(int iter, int day, int[] c) throws Exception {
		this.bw.write(iter + ","+ day);
		for(int i = 0; i <c.length; i++){
			this.bw.write(","+ c[i]);
		}
		this.bw.write(newline);
		this.bw.flush();
	}
	public void openWriteDemog() throws Exception {
		
		this.bw.write("iter,day,Chick,Grower,Hen,Cock,gSales,aSales,buys,eggs" + newline);
		this.bw.flush();
	}
	public void openWriteDemogABC() throws Exception {
		
		this.bw.write("iter,count,cN,hN,coN,cD,gD,aD,bR,hR,sG,sA,bA,Chick,Grower,Hen,Cock,gSalse,aSales,buys" + newline);
		this.bw.flush();
	}

	public void openWriteDemogND() throws Exception {
		
		this.bw.write("iter,day,Chick,Grower,Hen,Cock,births,");
		this.bw.write("dChick,dGrower,dHen,dCock,ndChick,ndGrower,ndHen,ndCock,notVacc,vaccFail,vaccExpire");
		this.bw.write(",cProtected,gProtected,aProtected");
		this.bw.write(",gSales,aSales,buys,clutches,eggs,newGrowers,newHens,newCocks,vaccinated" + newline);

		this.bw.flush();
	}

	public void openWriteSampleSize() throws Exception {
		
		this.bw.write("flock,iter,day,Chick,Grower,Hen,Cock,births,");
		this.bw.write("dChick,dGrower,dHen,dCock,ndChick,ndGrower,ndHen,ndCock,notVacc,vaccFail,vaccExpire");
		this.bw.write(",cProtected,gProtected,aProtected");
		this.bw.write(",gSales,aSales,buys,clutches,eggs,newGrowers,newHens,newCocks,vaccinated,Sales" + newline);

		this.bw.flush();
	}

	public void writeDemog(int iter, int day, int[] flock, int[] sales) throws Exception {

		this.bw.write(iter + ","+ day+ ","+ flock[0]+ ","+ flock[1]+ ","+ flock[2]+ ","+ flock[3] + "," + sales[0] + "," + sales[1] + "," + sales[2] + "," + sales[3]+ newline);

		this.bw.flush();
	}

	public void writeDemogND(int iter, int day, int[] flock, int births, int[] deaths, int[] deathsND, int[] deathsReason, int[] fProtected, int[] sales, int[] graduates, boolean vacc) throws Exception {

		this.bw.write(iter + ","+ day+ ",");
		this.bw.write(flock[0]+ ","+ flock[1]+ ","+ flock[2]+ ","+ flock[3] + ",");
		this.bw.write(births + ",");
		this.bw.write(deaths[0]+ ","+ deaths[1]+ ","+ deaths[2]+ ","+ deaths[3]+ ",");
		this.bw.write(deathsND[0]+ ","+ deathsND[1]+ ","+ deathsND[2]+ ","+ deathsND[3]+ ",");
		this.bw.write(deathsReason[0]+ ","+ deathsReason[1]+ ","+ deathsReason[2]+ ",");
		this.bw.write(fProtected[0]+ ","+ fProtected[1]+ ","+ fProtected[2]+ ",");
		this.bw.write(sales[0] + "," + sales[1] + "," + sales[2] + "," + sales[3]+ "," + sales[4]+ ",");
		this.bw.write(graduates[0] + ","+ graduates[1] + ","+ graduates[2] + ",");
		this.bw.write(vacc + newline);
		
		this.bw.flush();
	}
	public void writeTrojan(int iter, int day, Flock anni) throws Exception {

		this.bw.write(iter + ","+ day+ ",");
		int[] sales = anni.getSaleOutput();
		int[] flockOut = anni.getFlock();
		int[] deaths = anni.getDeathsAge();
		int[] ndDeaths = anni.getndDeathsAge();
		int[] ndDeathReason = anni.getndDeathsReason();
		int[] demProtected = anni.getProtected();
		int births = anni.getBirths();
		int[] graduates = anni.getGraduates();
		boolean vaccD = anni.getVaccDay() == day; 

		this.bw.write(flockOut[0]+ ","+ flockOut[1]+ ","+ flockOut[2]+ ","+ flockOut[3] + ",");
		this.bw.write(births + ",");
		this.bw.write(deaths[0]+ ","+ deaths[1]+ ","+ deaths[2]+ ","+ deaths[3]+ ",");
		this.bw.write(ndDeaths[0]+ ","+ ndDeaths[1]+ ","+ ndDeaths[2]+ ","+ ndDeaths[3]+ ",");
		this.bw.write(ndDeathReason[0]+ ","+ ndDeathReason[1]+ ","+ ndDeathReason[2]+ ",");
		this.bw.write(demProtected[0]+ ","+ demProtected[1]+ ","+ demProtected[2]+ ",");
		this.bw.write(sales[0] + "," + sales[1] + "," + sales[2] + "," + sales[3]+ "," + sales[4]+ ",");
		this.bw.write(graduates[0] + ","+ graduates[1] + ","+ graduates[2] + ",");
		this.bw.write(vaccD + newline);
		
		this.bw.flush();
	}
	public void writeTrojan(int iter, int day, Flock[] trojans) throws Exception {

		for(int i = 0; i < trojans.length; i++){
			Flock anni = trojans[i];
			this.bw.write((i + 1) + ","+ iter + ","+ day+ ",");
			int[] sales = anni.getSaleOutput();
			int[] flockOut = anni.getFlock();
			int[] deaths = anni.getDeathsAge();
			int[] ndDeaths = anni.getndDeathsAge();
			int[] ndDeathReason = anni.getndDeathsReason();
			int[] demProtected = anni.getProtected();
			int births = anni.getBirths();
			int[] graduates = anni.getGraduates();
			boolean vaccD = anni.getVaccDay() == day; 

			this.bw.write(flockOut[0]+ ","+ flockOut[1]+ ","+ flockOut[2]+ ","+ flockOut[3] + ",");
			this.bw.write(births + ",");
			this.bw.write(deaths[0]+ ","+ deaths[1]+ ","+ deaths[2]+ ","+ deaths[3]+ ",");
			this.bw.write(ndDeaths[0]+ ","+ ndDeaths[1]+ ","+ ndDeaths[2]+ ","+ ndDeaths[3]+ ",");
			this.bw.write(ndDeathReason[0]+ ","+ ndDeathReason[1]+ ","+ ndDeathReason[2]+ ",");
			this.bw.write(demProtected[0]+ ","+ demProtected[1]+ ","+ demProtected[2]+ ",");
			this.bw.write(sales[0] + "," + sales[1] + "," + sales[2] + "," + sales[3]+ "," + sales[4]+ ",");
			this.bw.write(graduates[0] + ","+ graduates[1] + ","+ graduates[2] + ",");
			this.bw.write(vaccD + "," + anni.getCumSales() + newline);
		}
		this.bw.flush();
	}

	
	public void writeDemogABC(int iter,int count, double[] params, int[] flock, int[] sales) throws Exception {
		this.bw.write(iter + "," + count);
		for(int i = 0; i <params.length; i++){
			this.bw.write(","+ params[i]);
		}
		for(int i = 0; i <flock.length; i++){
			this.bw.write(","+ flock[i]);
		}
		for(int i = 0; i <sales.length; i++){
			this.bw.write(","+ sales[i]);
		}
		this.bw.write(newline);
		this.bw.flush();
	}

}
