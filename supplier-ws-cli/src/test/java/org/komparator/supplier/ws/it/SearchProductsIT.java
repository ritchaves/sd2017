package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;


/**
 * Test suite
 */
public class SearchProductsIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
		
		client.clear();
		
		{
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Baseball ball");
			product.setPrice(11);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("X2");
			product.setDesc("Baseball bat");
			product.setPrice(69);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball golden ball");
			product.setPrice(9000);
			product.setQuantity(5);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("J1");
			product.setDesc("Soccer dragon ball z");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("H1");
			product.setDesc("Tennis ball");
			product.setPrice(9);
			product.setQuantity(100);
			client.createProduct(product);
		}
		
	}

	@AfterClass
	public static void oneTimeTearDown() {
		client.clear();
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	// tests
	// assertEquals(expected, actual);

	// public List<ProductView> searchProducts(String descText) throws
	// BadText_Exception

	

	//TO DO - joao- bad input tests

	@Test(expected = BadText_Exception.class)
	public void searchProductsEmptyTest() throws BadText_Exception {
		client.searchProducts("");
	}
	
	@Test(expected = BadText_Exception.class)
	public void searchProductsSpaceTest() throws BadText_Exception {
		client.searchProducts(" ");
	}
	
	@Test(expected = BadText_Exception.class)
	public void searchProductsLineTest() throws BadText_Exception {
		client.searchProducts("\n");
	}
	
	@Test(expected = BadText_Exception.class)
	public void searchProductsNullTest() throws BadText_Exception {
		client.searchProducts(null);
	}
	
	@Test(expected = BadText_Exception.class)
	public void searchProductsTabTest() throws BadText_Exception {
		client.searchProducts("\t");
	}
	
	

	// main tests
	@Test
	public void searchProductExistsTest() throws BadText_Exception {
		List<ProductView> products = client.searchProducts("Baseball");	
		assertFalse(products.isEmpty());
		assertEquals(3, products.size());
		
		for (ProductView pds : products) {
			assertThat(pds.getDesc(), containsString("Baseball"));
		}
		
	}
	
	@Test
	public void searchProductAnotherExistsTest() throws BadText_Exception {
		List<ProductView> products = client.searchProducts("Soccer");	
		assertFalse(products.isEmpty());
		assertEquals(2, products.size());
		
		
		for (ProductView pds : products) {
			assertThat(pds.getDesc(), containsString("Soccer"));
		}
	}
			
	@Test
	public void searchProductYetAnotherExistsTest() throws BadText_Exception {
		List<ProductView> products = client.searchProducts("Tennis");	
		assertFalse(products.isEmpty());
		assertEquals(1, products.size());
		
		
		for (ProductView pds : products) {
			assertThat(pds.getDesc(), containsString("Tennis"));
		}
	}
	
	@Test
	public void searchProductExactNameWithSpacesTest() throws BadText_Exception {
		List<ProductView> products = client.searchProducts("Baseball bat");	
		assertFalse(products.isEmpty());
		assertEquals(1, products.size());
		
		for (ProductView pds : products) {
			assertThat(pds.getDesc(), containsString("Baseball bat"));
		}
	}
	
	@Test
	public void searchProductNotExistsTest() throws BadText_Exception {
		List<ProductView> products = client.searchProducts("Olaerasoiloioi");
		assertTrue(products.isEmpty());
	}
	
	@Test
	public void searchProductLowerCaseNotExistsTest() throws BadText_Exception {
		List<ProductView> products = client.searchProducts("baseball");
		assertTrue(products.isEmpty());
	}
}
