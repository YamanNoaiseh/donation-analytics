> 
> ORIGINAL PROBLEM SPECIFICATIONS
> https://github.com/InsightDataScience/donation-analytics
>


# Author: Yaman Noaiseh


# Implementation Tools:
- Tools used: Java SE 8 on Eclipse Jee Oxygen

- External libraries: None.

# Data structures:
•	HashMap<String, DonationsCollection>: Where the key is the concatination of "CMTE_ID" and "-" and "5-digits-ZIP_CODE", and the value is an object of custom class described below.
•	HashSet<String>: to keep track of repeat donors. An element of this set is made of the concatination of the donor's "NAME" and "-" and "5-digits-ZIP_CODE" as a unique identifier of a donor.

Each communication with these hash-based datastructures is constant time.

# Class DonationsCollection:  
This class keeps track of the total amounts have been donated by repear donors in the current year from the current zip code, as well as the total number of donations made, and calculates the running percentile of donations from repeat donors.  
It keeps two heaps (Java PriorityQueue objects):  
lowHeap: A max heap  
highHeap: A min heap  
and ensures that lowHeap keeps number of elements equals to the ordinal rank of the running percentile according to the nearest-rank method.  
Each addition to this heap-based datastructure is O(log(K)) time complexity, where K is the total number of elements in both heaps in this specific DonationsCollection object.


# Assumptions:  
•	Minimum length of the "NAME" field is 2. That ism when the name contains only first initials.  
•	The second last element in an output line (total amount of contributions received) is a decimal value (Java double) unless it is actually an integer, which is then outputted with no decimal point.  
Example 1: if the total amount of contributions received = 1000.11 it will be presented as 1000.11  
Example 2: if the total amount of contributions received = 1000.00 it will be presented as 1000
