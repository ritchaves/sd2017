package org.komparator.mediator.ws.it;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;


/**
 * Test suite
 */
public class AddToCartIT extends BaseIT {

	private static final String X1 = "X1";
	private static final String X2 = "X2";
	private static SupplierClient sp1;
	private static SupplierClient sp2;
	private static SupplierClient sp3;
	static ItemIdView itemidview = null;
	static ItemIdView itemidview2 = null;
	static ItemIdView itemidview3 = null;
	static ItemIdView baditemidview1 = null;
	static ItemIdView baditemidview2 = null;
	static ItemIdView baditemidview3 = null;
	static ItemIdView baditemidview4 = null;
	static ItemIdView baditemidview5 = null;


	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws InvalidItemId_Exception, SupplierClientException, BadProductId_Exception, BadProduct_Exception {
		
		mediatorClient.clear();
		sp1.clear();
		sp2.clear();
		sp3.clear();
		
		
		{
			sp1 = new SupplierClient("http://localhost:8081/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc("Puppy");
			product.setPrice(5);
			product.setQuantity(10);
			sp1.createProduct(product);
			
			itemidview = new ItemIdView();
			itemidview.setProductId(X1);
			itemidview.setSupplierId("81");
		}
		{
			sp2 = new SupplierClient("http://localhost:8082/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X2);
			product.setDesc("Kitten");
			product.setPrice(15);
			product.setQuantity(20);
			sp2.createProduct(product);
			
			itemidview2 = new ItemIdView();
			itemidview2.setProductId(X2);
			itemidview2.setSupplierId("82");
		}
		{
			sp3 = new SupplierClient("http://localhost:8083/supplier-ws/endpoint");
			
			ProductView product = new ProductView();
			product.setId(X1);
			product.setDesc("Other sort of puppy");
			product.setPrice(10);
			product.setQuantity(20);
			sp3.createProduct(product);
			
			itemidview3 = new ItemIdView();
			itemidview3.setProductId(X1);
			itemidview3.setSupplierId("83");
		}	
		
		//BAD Item ID:
		baditemidview1 = new ItemIdView();
		baditemidview1.setProductId(null);
		baditemidview1.setSupplierId("82");
		
		baditemidview2 = new ItemIdView();
		baditemidview2.setProductId("");
		baditemidview2.setSupplierId("82");
		
		baditemidview3 = new ItemIdView();
		baditemidview3.setProductId(" ");
		baditemidview3.setSupplierId("82");
		
		baditemidview4 = new ItemIdView();
		baditemidview4.setProductId("\t");
		baditemidview4.setSupplierId("82");
		
		baditemidview5 = new ItemIdView();
		baditemidview5.setProductId("\n");
		baditemidview5.setSupplierId("82");
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		mediatorClient.clear();
		sp1.clear();
		sp2.clear();
		sp3.clear();
	}
	
	//CartId Tests
	@Test(expected = InvalidCartId_Exception.class)
	public void addToCartNullCartIdTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(null, itemidview2, 5);
	}

	@Test(expected = InvalidCartId_Exception.class)
	public void addToCartEmptyNameCartIdTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("", itemidview2, 5);
	}
	
	@Test(expected = InvalidCartId_Exception.class)
	public void addToCartWhiteSpaceNameCartIdTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(" ", itemidview2, 5);
	}
	
	@Test(expected = InvalidCartId_Exception.class)
	public void addToCartTabNameCartIdTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("\t", itemidview2, 5);
	}
	
	@Test(expected = InvalidCartId_Exception.class)
	public void addToCartNewLineNameCartIdTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("\n", itemidview2, 5);
	}

	/***/
	//Product Id tests
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartNullTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC1", null, 5);
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartOtherKindOfNullTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC1", baditemidview1, 5);
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartEmptyNameTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC1", baditemidview2, 5);
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartWhiteSpaceNameTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC1", baditemidview3, 5);
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartTabNameTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC1", baditemidview4, 5);
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartNewLineNameTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC1", baditemidview5, 5);
	}
	
	/***/
	//Quantity Test
	@Test(expected = InvalidQuantity_Exception.class)
	public void addToCartInvalidQuantity() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC1", itemidview2, -5);
	}
	
	@Test(expected = InvalidQuantity_Exception.class)
	public void addToCartZeroQuantity() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC1", itemidview2, 0);
	}
	
	/***/
	//Function tests
	@Test
	public void addCartNewCartSuccess() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC1", itemidview, 1);
		
		//TODO Assert me!
		
	
	}
	
	@Test
	public void addCartExistentCartSuccess() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC1", itemidview, 1);
		
		//TODO Assert me!
		
	
	}
	
	@Test
	public void addCartTwoProductsSuccess() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC2", itemidview3, 1);
		mediatorClient.addToCart("SC2", itemidview2, 1);
	
		//TODO Assert me!
	}
	
	@Test
	public void addCartSameProductTwiceSuccess() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC3", itemidview2, 3);
		mediatorClient.addToCart("SC3", itemidview2, 5);
		//TODO Assert me!
	}
	
	/***/
	@Test(expected = NotEnoughItems_Exception.class)
	public void addCartSameProductTwiceNotEnoughInStockFailure() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("SC4", itemidview, 4);
		mediatorClient.addToCart("SC4", itemidview, 7);
	
	}
}
	
	
	