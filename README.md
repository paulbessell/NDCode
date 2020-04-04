# NDCode
Code for GalvMed ND model

Written by Paul Bessell paul.bessell@roslin.ed.ac.uk for simulating Newcastle disease spread in chicken flocks.

The model is called from the dirverAfrica.java which contains the main string arguments, but this initiates a Population object which calls the remaining classes. The Population creates a population of Villag objects which contain Flock objects which contain Bird objects. The class NDInfection models the parameters of the Newcastle disease virus

The model models the spread of Newcastle disease through birds in flocks, between flocks and between villages over a defined number of time steps.

The model also manages the population so chickens are born, consumed, sold and traded between villages.

The class ReadWrite manages input and output
