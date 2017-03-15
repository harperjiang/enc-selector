# Encoding Selector
This project explores efficient lightweight encoding for column-based databases.
## Dataset Collection

We collect over 7000 columns extracted from over 1200 datasets with a total size of over 500G data. These datasets are all from real-world datasources and contain various data types (integer, date, address, etc.). They are a good representation of real-world data distribution.

We use Apache Parquet to encode these data columns with different encoding schemes, looking for the one performing best for each column. 

## Pattern Extraction
Data type is crucial to a proper encoding selection. For example, storing a date info in string format requires at least 8 bytes, while storing it in integer format takes no more than 4 bytes. If we further observe the effective data range, this can further be reduced to 23 bit per record. 

### Subtypes
Besides the common known data type such as string, integer and double, there are sub-types that can achieve further compression. For example, a typical address has the following format: 1234 [NSEW] ABC [ROAD|BOULEVARD|STREET], CITY, STATE, ZIP. If encoded as string, this takes in average 30-50 bytes. While a smart encoding will take only half of it.

Determination of sub-types involves first tokenizing the string and then match them with maximal likelihood on each separation point. Given a set of strings, we first tokenize them using common separators such as space, comma and slash/backslash. In the ideal case, all data will have same number of tokens, which allows us to naturally match them and check the possibility of performing compression. However, in most cases the number of tokens from different lines will not be the same, which will require a matching / partitioning between fields. 

We execute the following steps to look for potential patterns:
* Explicit separators such as comma, colon and semicolons have highest priority. 
* Separator separating different data types, e.g. Integer - String will be honored
* Common Words will be 

## Data Driven Encoding Prediction

Given a data column, we assume the following features are available 
* Column name
* Limited samples of data (first several rows)

Sometimes column name contains keywords that can be used to infer data type