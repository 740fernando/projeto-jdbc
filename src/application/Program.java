package application;

import java.util.Date;

import model.dao.DaoFactory;
import model.dao.SellerDao;
import model.entitites.Department;
import model.entitites.Seller;

public class Program {

	public static void main(String[] args) {
			
		SellerDao sellerDao = DaoFactory.createSellerDao();
		System.out.println("============= Teste 1 : seller findById =============");
			
		Seller seller = sellerDao.findById(3);		
		System.out.println(seller);
	}		
}