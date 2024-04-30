
# Sherlock3
Katelyn Rohrer, Lydia Dufek, Camila Grubb


We're running a Maven build for the Java code in the project.
For the improved implementation, we added Python to the project,
which is being managed by a virtual environment.

## [Link to Index](https://drive.google.com/drive/folders/1AdFS6eOTPfj-YmSm3ZjNAa3LLkpr80xM?usp=sharing)


## Executing Java code (initial implementation)

### Building and running the index
`mvn exec:java -D"exec.mainClass=model.Index"`

##### Output
The index is written to `sherlock3/IndexBuild`


### Building and running the query
`mvn exec:java -D"exec.mainClass=model.Query"`

##### Output
The results are written to `sherlock3/answers.txt` and also copied
to `sherlock3/src/main/python/answers.txt`



## Executing Python code (improved implementation)

### For GPT Implementation, a venv and an API key are required.

#### Configuring the venv:
`cd src/main/python` 

`python3 -m venv venv`

##### For Windows: `.\venv\Scripts\activate`
##### For Unix or MacOS: `source venv/bin/activate`

#### Adding the API Key:
Add the API key to `src/main/python/empty_constants.py` as `API_KEY`

#### Install dependencies:
`pip3 install -r requirements.txt`

#### Run the program:
`python3 gpt.py`

##### Output
The results are written to `sherlock3/src/main/python/GPT_answers[3.5/4.0].txt`