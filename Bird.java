/****************************************
 *  
 * Class that deals with all methods for individual birds 
 * 
 */

import java.util.*;
import org.apache.commons.math3.distribution.*;

public class Bird {
//	Relatively few instance variables
	private int age;
	private boolean female;
//	Used to differentiate chicks / growers / hens / cocks
	private String type;
//	Upper age limits for chicks and growers	
	private int chickAge;
	private int growerAge;
//	These all relate to ND infection	
	public NDInfection NDInf;
	private boolean isVirus;
	public boolean isProtected;
	public boolean isVaccinated;
	private boolean isVaccineFail;
	private boolean nonFatal;
	private boolean vaccBird;

// Overloaded constructor for seeding a new flock
	public Bird(String type){
		this.type = type;
// Maximum age for a chick or a grower		
		this.chickAge = 42;
		this.growerAge = 180;
		this.growerAge = 150;
		this.setAge();
	}
	// Overloaded constructor for a newborn
	public Bird(){
		this.age = 0;
		this.female = Math.random() >= 0.5;	
		this.type = "Chick";
		this.chickAge = 42;
		this.growerAge = 180;
		this.growerAge = 150;
	}
	// Overloaded constructor to fix maximum ages
	public Bird(int chickAge, int growerAge){
		this.age = 0;
		this.female = Math.random() >= 0.5;	
		this.type = "Chick";
		this.chickAge = chickAge;
		this.growerAge = growerAge;
	}

	// Overloaded constructor to create a "new" adult bird	
	public Bird(boolean female){
		this.chickAge = 42;
		this.growerAge = 180;
		this.growerAge = 150;
		this.age = this.growerAge+1;
		this.female = female;
		this.type = "Cock";
		if(this.female) this.type = "Hen";	
	}
//	Set random ages for new bird - when the flock is initiated	
	private void setAge(){
		if(this.type == "Chick"){
			int[] ages = this.getChickAgeArray();
			double[] ageProbs = this.getAgeProbs(ages);
			EnumeratedIntegerDistribution chickSamp = new EnumeratedIntegerDistribution(ages, ageProbs);
			this.age = chickSamp.sample();
		}
		if(this.type == "Grower"){
			int[] ages = this.getGrowerAgeArray();
			double[] ageProbs = this.getAgeProbs(ages);
			EnumeratedIntegerDistribution chickSamp = new EnumeratedIntegerDistribution(ages, ageProbs);
			this.age = chickSamp.sample();			
		}
// Define whether the chick or grower is female
		this.female = Math.random() > 0.5;
		if(this.type == "Hen"){
			this.age = this.growerAge + 1;
			this.female = true;
		}
		if(this.type == "Cock"){
			this.age = this.growerAge + 1;
			this.female = false;
		}
	}

// Three methods for creating the bird age sampling framework
	private int[] getChickAgeArray(){
		int[] ages = new int[this.chickAge];
		for(int i = 0; i < this.chickAge; i++) ages[i] = i;
		return ages;
	}

	private int[] getGrowerAgeArray(){
		int[] ages = new int[this.growerAge - this.chickAge];
		for(int i = this.chickAge; i < this.growerAge; i++) ages[i - this.chickAge] = i;
		return ages;
	}

//	Age based probability array
	private double[] getAgeProbs(int[] ages){
		double[] ageProbs = new double[ages.length];
		int ageSum = 0;
		for(int i = 0; i < ages.length; i++) {
			ageSum = ageSum + ages[i];
		}
		double ageSumD = (double) ageSum;
		for(int i = 1; i <= ages.length ; i++){
			double cAge = (double) ages[ages.length - i];
			ageProbs[i-1] = cAge / ageSumD; 
		}
		return ageProbs;
	}

	public boolean isFemale(){
		return this.female; 
	}
	
	public int getAge(){
		return this.age;
	}
//	Increment the age of birds and if necessary "promote" them
	public void incrementAge(double propHen){
		this.age++;
		if(this.age == this.chickAge && this.age < this.growerAge) this.type = "Grower";
		if(this.age == this.growerAge) this.female = propHen > Math.random(); 
		if(this.age >= this.growerAge && this.female) this.type = "Hen";
		if(this.age >= this.growerAge && !this.female) this.type = "Cock";
	}
	
	public String getType(){
		return this.type;
	}
	
//	Set infection with ND
	public void setNDInfected(NDInfection newcastleDisease){
		this.nonFatal = this.testVirus();
		this.NDInf = new NDInfection(newcastleDisease);
		this.isVirus = true;
		this.isProtected = true;
	}

//	Set the ND vaccination status of the bird	
	public void setNDVaccinated(NDInfection newcastleDisease){
		if(this.vaccineEligible()){
//	Bodged - the duration of immunity of the vaccine is hard-coded here
			this.NDInf = new NDInfection(120);
			this.isVaccinated = true;
			this.isProtected = true;
			this.isVaccineFail = false;
			this.isVirus = false;
		}
	}
//	If the bird is already infected it is ineligible for vaccine
	private boolean vaccineEligible(){
		boolean eligible = true;
		if(this.testVirus()){
			if(this.NDInf.getStatus() == "Latent" | this.NDInf.getStatus() == "Infectious") eligible = false;
		}
		return eligible;
	}

//	Step through the ND compartments	
	public boolean incrementND(){
		boolean dead = false;
		String ndStatus = this.NDInf.incrementDay(this.getType(), this.nonFatal);
		if(ndStatus == "Dead") dead = true;
		if(ndStatus == "Recovered" | ndStatus == "Nil") this.isProtected = false;
		if(ndStatus == "Nil") this.isVirus = false;
		
		return dead;
	}
	public boolean testVirus(){
		return this.isVirus;
	}
	public boolean testProtected(){
		return this.isProtected;
	}
	public boolean testVaccinated(){
		return this.isVaccinated;
	}
	public void setVaccineFail(){
		this.isVaccineFail = true;
		this.vaccBird = true;
	}
	public boolean testVaccineFail(){
		return this.isVaccineFail;
	}
	public void setVaccBird(){
		this.vaccBird = true;
	}
	public boolean getVaccBird(){
		return this.vaccBird;
	}
	public double getInfTrans(){
		double infTrans = 1.1;
		if(this.type == "Chick") infTrans = 0.6;
		if(this.type == "Grower") infTrans = 0.9;
		return infTrans;
	}
	public boolean betweenFlock(){
		boolean bf = Math.random() < this.getInfTrans();
		return bf;
	}

}
