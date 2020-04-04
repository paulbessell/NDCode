/******************************
 * 
 * This Class is methods for dealing with ND infections
 * 
 ****************************/

import org.apache.commons.math3.distribution.*;

public class NDInfection {
//	Instance variables for infection
	private final String type;
	private final int latent;
	private final int infectious;
	private final int pTitre;
	private final int titre;
	private final int maternalA;
	private final double mortality;
	private final double transmission;

//	Sampled instance variables for infection compartments	
	private int latentS;
	private int infectiousS;
	private int pTitreS;
	private int titreS;
	
// Counters for the remaining time in each compartment	
	private int latentRem;
	private int infRem;
	private int pTitreRem;
	private int titreRem;
	private String status;
	private boolean nonFatal;

//	Overloaded constructor to initiate an infection with fixed periods.	
	public NDInfection(String type, int latent, int infectious, int pTitre, int titre, int maternalA, double mortality, double transmission){
		this.type = type;
		this.latent = latent;
		this.infectious = infectious;
		this.pTitre = pTitre;
		this.titre = titre;
		
		this.maternalA = maternalA;
		this.mortality = mortality;
		this.transmission = transmission;
	}

//	Initiate an infection based on the properties of an existing virus	
	public NDInfection(NDInfection ndVirus){
		this.type = ndVirus.getType();
		this.latent = ndVirus.getLatent();
		this.infectious = ndVirus.getInfectious();
		this.pTitre = ndVirus.getProtective();
		this.titre = ndVirus.getTitre();
		
//	Sample the infection compartments		
		this.latentS = new PoissonDistribution(this.latent).sample();
		this.infectiousS = new PoissonDistribution(this.infectious).sample();
		this.pTitreS = new PoissonDistribution(this.pTitre).sample();
		this.titreS = new PoissonDistribution(this.titre).sample();

		this.maternalA = ndVirus.getMatA();
		this.mortality = ndVirus.getMortality();
		this.transmission = ndVirus.getTransmission();
		this.kickOff();
	}

//	Overloaded constructor for a vaccine virus - not really used	
	public NDInfection(int pTitre){
		this.type = "Vaccine";
		this.latent = 1;
		this.infectious = 0;
		this.pTitre = pTitre;
		this.titre = 0;
		this.transmission = 0;
		this.mortality = 0;
		this.maternalA = 0;
		
		this.latentS = new PoissonDistribution(this.latent).sample();
		this.infectiousS = this.infectious;
		this.pTitreS = new PoissonDistribution(this.pTitre).sample();
		this.titreS = this.titre;
		this.kickOff();
//		this.status = "Nil";
	}

	public String getType(){
		return this.type;
	}
	private int getLatent(){
		return this.latent;
	}
	private int getInfectious(){
		return this.infectious;
	}
	private int getProtective(){
		return this.pTitre;
	}
	private int getTitre(){
		return this.titre;
	}
	private int getMatA(){
		return this.maternalA;
	}
	private double getMortality(){
		return this.mortality;
	}
	public double getTransmission(){
		return this.transmission;
	}
	
	public String getStatus(){
		return this.status;
	}
	
	private void kickOff(){
		this.latentRem = this.latentS;
		this.status = "Latent";
	}

//	This adjusts the mortality rates for growers and adults	
	public boolean getDead(String type){
		boolean dead = false;
		double mortalityRate = this.getMortality();
		if(type != "Chick") mortalityRate *= 0.8;
		if(type == "Hen") mortalityRate *= 0.8;
		if(type == "Cock") mortalityRate *= 0.8;
		dead = mortalityRate > Math.random();

		return dead;
	}

//	Loops through the compartments and where necessary moves birds between compartments	
	public String incrementDay(String type, boolean nonFatal){
		if(this.status == "Recovered"){
			this.titreRem--;
			if(this.titreRem == 0){
				this.status = "Nil";
			}
			if(this.titreRem < 0) System.out.println("Here");
		}
		if(this.status == "Protected"){
			this.pTitreRem--;
			if(this.pTitreRem == 0){
				this.status = "Recovered";
				if(this.type == "Vaccine") this.status = "Nil";

				this.titreRem = this.titreS - this.pTitreS;
			}
		}
		if(this.status == "Infectious"){
			this.infRem--;
			if(this.infRem <= 0){
				this.status = "Protected";
				this.pTitreRem = this.pTitreS;
			}
			else if(this.infRem > 0 & !nonFatal){
					if(this.getDead(type)){
					this.status = "Dead";
				}
			}
		}
		if(this.status == "Latent"){
			this.latentRem --;
			if(this.latentRem <= 0){
				this.status = "Infectious";
				this.infRem = this.infectiousS;
				if(this.getDead(type) && this.type != "Vaccine"){
					this.status = "Dead";
				}
			}
		}
		return this.getStatus();
	}

}
