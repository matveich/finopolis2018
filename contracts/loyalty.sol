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
    
    // bank address
    address public owner;
    
    
    mapping (address => Customer) public customers;
    mapping (address => Coalition.Company) public companies;
    
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
                //uint roublesAmount
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
