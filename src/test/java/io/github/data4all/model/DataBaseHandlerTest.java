package io.github.data4all.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.github.data4all.model.DataBaseHandler;
import io.github.data4all.model.data.Node;
import io.github.data4all.model.data.PolyElement;
import io.github.data4all.model.data.User;
import io.github.data4all.model.data.PolyElement.PolyElementType;
/**
 * This class tests the DataBaseHandler.
 * 
 * @author Kristin Dahnken
 *
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class DataBaseHandlerTest {
	
	private DataBaseHandler dbHandler;
			
	@Before
	public void setUp(){
		dbHandler = new DataBaseHandler(Robolectric.application);
		System.out.println("Setup done.");
	}
	
	@Test
	public void testUserCRUD(){		
		
		User user1 = new User("Liadan", "abcde12345", "xyz");
		User user2 = new User("Manus", "fgh81375", "abc");
		
		dbHandler.createUser(user1);
		dbHandler.createUser(user2);
		
		assertEquals("abcde12345", dbHandler.getUser("Liadan").getOAuthToken());
		assertEquals("abc", dbHandler.getUser("Manus").getOauthTokenSecret());		
	
		user1.setOAuthToken("xyz97");
		user2.setOauthTokenSecret("ads8790");
		
		dbHandler.updateUser(user1);
		dbHandler.updateUser(user2);
		
		assertEquals("xyz97", dbHandler.getUser("Liadan").getOAuthToken());
		assertEquals("ads8790", dbHandler.getUser("Manus").getOauthTokenSecret());
		
		dbHandler.deleteUser(user1);
		dbHandler.deleteUser(user2);
		
		assertEquals(0, dbHandler.getUserCount());
	}
	
	@Test
	public void testNodeCRUD(){
		
		Node node1 = new Node(1, 30.123456, 40.1234567);
		Node node2 = new Node(2, 25.982423, 42.7483024);
		Node node3 = new Node(3, 23.325786, 41.0457094);
		
		dbHandler.createNode(node1);
		dbHandler.createNode(node2);
		dbHandler.createNode(node3);
		
		assertEquals(30.123456, dbHandler.getNode(1).getLat(), 0.0);
		assertEquals(25.982423, dbHandler.getNode(2).getLat(), 0.0);
		assertEquals(41.0457094, dbHandler.getNode(3).getLon(), 0.0);
		
		node1.setLat(31.123456);
		node2.setLat(26.986764);
		node3.setLon(42.869686);
		
		dbHandler.updateNode(node1);
		dbHandler.updateNode(node2);
		dbHandler.updateNode(node3);
		
		assertEquals(31.123456, dbHandler.getNode(1).getLat(), 0.0);
		assertEquals(26.986764, dbHandler.getNode(2).getLat(), 0.0);
		assertEquals(42.869686, dbHandler.getNode(3).getLon(), 0.0);
		
		dbHandler.deleteNode(node1);
		dbHandler.deleteNode(node2);
		dbHandler.deleteNode(node3);
		
		assertEquals(0, dbHandler.getNodeCount());
	}
	
	@Test
	public void testPolyElementCRUD() throws JSONException{
	    
	    PolyElement polyElement1 = new PolyElement(1, PolyElementType.AREA);
	    PolyElement polyElement2 = new PolyElement(2, PolyElementType.BUILDING);
	    PolyElement polyElement3 = new PolyElement(3, PolyElementType.WAY);
	    
	    Node node1 = new Node(4, 30.123456, 40.1234567);
        Node node2 = new Node(5, 25.982423, 42.7483024);
        Node node3 = new Node(6, 23.325786, 41.0457094);
        Node node4 = new Node(7, 34.744587, 41.0937468);
        Node node5 = new Node(8, 28.935876, 45.6767676);
        Node node6 = new Node(9, 33.333333, 44.4444444);
        
        polyElement1.addNode(node1);
        polyElement1.addNode(node2);
        
        polyElement2.addNode(node3);
        polyElement2.addNode(node4);
        
        polyElement3.addNode(node5);
        polyElement3.addNode(node6);
        
        dbHandler.createPolyElement(polyElement1);
        dbHandler.createPolyElement(polyElement2);
        dbHandler.createPolyElement(polyElement3);
        
//        assertEquals(3, dbHandler.getPolyElementCount());
        
	}
	
	@After
	public void tearDown(){
	    dbHandler.close();
	}
}
