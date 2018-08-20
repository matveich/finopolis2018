pragma solidity ^0.4.0;

import "./coalition.sol";
import "./token.sol";

contract Loyalty {
    using SafeMath for uint;
    struct Customer {
        bool exists;
        uint phoneNumber;
        mapping (address => bool) tokens;
    }
    
    struct Company {
        bool exists;
        Token token;
        string name;
        uint phoneNumber;
        Request[] request_pool; 
        mapping (address => bool) coalitions;
    }
    
    struct Request {
        string message;
        address sender;
        RequestType _type;
    }
    
    struct Coalition{
        bool exists;
        string name;
        address leader;
        mapping (address => bool) members;
    }
    
    enum RequestType {INVITE}
    
    // bank address
    address public owner;
    
    
    mapping (address => Customer) public customers;
    mapping (address => Company) public companies;
    mapping (address => Coalition) public coalitions;

    
    // map from company (owner) address to Token
    mapping (address => Token) public allTokens;
    
    // events for debug and output
    event AddCompany(address companyAddress, string name, uint phoneNumber);
    event AddCustomer(address customerAddress, uint number);
    event LoggedIn(address _address, uint number);
    
    constructor() public {
        owner = msg.sender;
    }
    
    // bank calls
    function addCustomer(address customer, uint _phoneNumber) public
                onlyOwner
                customerNotExists(customer)
                companyNotExists(customer) {
        customers[customer].exists = true;
        customers[customer].phoneNumber = _phoneNumber;
        emit AddCustomer(customer, customers[customer].phoneNumber);
    }
    
    // bank calls
    function addCompany(address company, string _name, uint _phoneNumber) public
                onlyOwner
                companyNotExists(company)
                customerNotExists(company) {
        companies[company].exists = true;
        companies[company].name = _name;
        companies[company].phoneNumber = _phoneNumber;
        emit AddCompany(company, companies[company].name, customers[company].phoneNumber);
    }
    
    // bank calls
    function transferBonuses(address company,
                             address customer,
                             uint roublesAmount,
                             uint bonusesAmount,
                             address tokenOwner) // 0 if bonusesAmount == 0
                                public
                                onlyOwner
                                customerExists(customer)
                                companyExists(company) returns (uint) // if bonusesAmount == 0 returns charged bonuses amount,
                                                                      // in another case returns roubles amount
                                {
        Token token = companies[company].ownToken;
        // charge bonuses to customer                            
        if (bonusesAmount == 0) {
            
            // the simpliest case - when token belongs to the company
            if (token.owner() == company) {
                uint tokensAmount = roublesAmount.mul(token.inPrice());
                token.transfer(company, customer, tokensAmount);
                customers[customer].tokens[token] = true;
                return tokensAmount;
            }
            else {
                // TODO: hard case, needs merge with Slavique
            }
        }
        // write off bonuses
        else {
            if (token.owner() == company) {
                uint deltaMoney = tokensAmount.mul(token.outPrice());
                roublesAmount = roublesAmount.add(deltaMoney);
                token.transfer(customer, company, tokensAmount);
            }
            else {
                // TODO: hard case, needs merge with Slavique
            }
        }
    }
    
    // company calls
    // name of the token, tokens per spent rouble, price when you spend tokens
    function createToken(Token token) public
        companyExists(msg.sender) {
        // doesn't exist
        require(companies[msg.sender].ownToken.owner() == address(0));
        require(token.owner() == msg.sender);
        companies[msg.sender].ownToken = token;
        allTokens[msg.sender] = token;
    }
    
    function addCoalition(address coalition, string _name) public 
                                companyExists(msg.sender)
                                coalitionNotExists(coalition){
        coalitions[coalition].exists = true;
        coalitions[coalition].name = _name;
        coalitions[coalition].members[msg.sender] = true;
        coalitions[coalition].leader = msg.sender;
        companies[msg.sender].coalitions[coalition] = true;
    }
    
    function inviteToCoalition(address coalition) public 
                                companyExists(msg.sender)
                                coalitionExists(coalition){
        Request join_request;
        join_request.message = "Idi nahui gomofobny pidaras";
        join_request.sender = msg.sender;
        join_request._type = RequestType.INVITE;
        companies[coalitions[coalition].leader].request_pool.push(join_request);
    }
    
    function getRequest () public // call while not tresnesh' 
                            companyExists(msg.sender)
                            {
        var request = companies[msg.sender].request_pool[companies[msg.sender].request_pool.length - 1];
        //return (request.message, request.sender);
    }
    
    modifier onlyOwner() {
        require(msg.sender == owner);
        _;
    }
    
    modifier customerExists(address customer) {
        require(customers[customer].exists, "Customer doesn't exist.");
        _;
    }
    
    modifier companyExists(address company) {
        require(companies[company].exists, "Company doesn't exist.");
        _;
    }
    
    modifier customerNotExists(address customer) {
        require(!customers[customer].exists, "Customer already exists.");
        _;
    }
    
    modifier companyNotExists(address company) {
        require(!companies[company].exists, "Company already exists.");
        _;
    }
}
