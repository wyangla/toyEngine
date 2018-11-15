# toyEngine
The toyEngine is a implementation of the search engine invert-index, along with basic and advanced functionalities. Specifically this program is currently providing **functionalities** including:  
1. persisting and loading of lexicon / last posting unit id / term associated information / added document information; 
2. posting list inependent persisting and lazily loading;   
3. double layers term lock service
4. posting unit adding, lazily deleting and posting list cleaning;  
5. document adding and deleting;  
6. posting list accessing status recording and automatically deactivating;  
7. inverted index reloading for garbage collection and reallocating post unit IDs;  
8. posting list scanning and simple document scoring models;  
9. three searching algorithms including plain search, maxScore and WAND;  
    
From the perspective of design, the program mainly consists of three parts,  
1. inverted-index and associated operations; 
2. entities supporting the implementations of the operations;  
3. helper classes like commonly used basic data structures and various utils.  
    
The design of entities are mostly applying the scheme of “**mainstay and plugins**”, in which specific functionalities and data structures are provided and maintained by the plugins, this is for the convenience of developing additional functionalities based on the current backbone.  

#### More details please refer to /Something_about_toyEngine.pdf .  
#### The operator could be found at https://github.com/wyangla/toyEngine_operator .  
#### What need to be noted is currently the py4j.GatewayServer is used to connect the operator and toyEngine.  

![toyEngine_architecture.jpg](./figs/toyEngine_architecture.jpg)