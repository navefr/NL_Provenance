package Circuit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import Basics.*;

public class Circuit 
{
	Vector<HashMap<String, Atom>> circuit = new Vector<HashMap<String, Atom>>();
	
	Map<String, Atom> mostRecentOrNodes = new HashMap<String, Atom>(); 
	
	public Circuit () 
	{
		this.circuit.add(new HashMap<String, Atom>()); //first layer for DB atoms
	}
	
	
	public Circuit (Atom root, Map<Atom, HashSet<Body>> provenance) 
	{
		this.circuit.add(new HashMap<String, Atom>()); //first layer for DB atoms
		BuildCicuitTopDown(root, provenance);
	}
	
	
	
	public Vector<HashMap<String, Atom>> getCircuit() 
	{
		return circuit;
	}


	
	public Map<String, Atom> getMostRecentOrNodes() 
	{
		return mostRecentOrNodes;
	}


	/*************************************************************************************************************/
	/** Title: BuildCicuitTopDown																				
	/** Description: 			
	/*************************************************************************************************************/
	
	public void BuildCicuitTopDown (Atom root, Map<Atom, HashSet<Body>> provenance)
	{
		Vector<Atom> workQueue = new Vector<Atom>();
		Vector<Atom> restQueue = new Vector<Atom>();
		workQueue.add(root);
		this.circuit.lastElement().put(root.toString(), root);
		this.circuit.add(new HashMap<String, Atom>());
		int layerNum = 0;
		
		while (false == workQueue.isEmpty() && layerNum < 10)
		{
			Atom curAtom = workQueue.remove(0);
			AddNodeToCircuit(curAtom, provenance.get(curAtom), provenance, restQueue, false, layerNum);
			AddQueueToCircuit(restQueue);
			if (true == workQueue.isEmpty()) 
			{
				layerNum++;
				this.circuit.add(new HashMap<String, Atom>());
				workQueue = (Vector) restQueue.clone();
				restQueue.clear();
			}
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: AddQueueToCircuit																				
	/** Description: Adds the element of queue to the circuit			
	/*************************************************************************************************************/
	
	public void AddQueueToCircuit (Vector<Atom> queue)
	{
		for (Atom atom : queue) 
		{
			if (false == this.circuit.lastElement().containsKey(atom.toString())) 
			{
				this.circuit.lastElement().put(atom.toString(), atom);
			}
			
			Atom existingAtom = this.circuit.lastElement().get(atom.toString());
			if (false == existingAtom.getParents().equals(atom.getParents()))
			{
				for (Atom parent : atom.getParents()) 
				{
					existingAtom.AddParent(parent);
					parent.getChildren().add(existingAtom);
				}
			}
			
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: AddToCircuitOffline																				
	/** Description: Adds and Nodes to prov.			
	/*************************************************************************************************************/
	
	public void AddToCircuitOffline (Atom key, HashSet<Atom> andNodes)
	{
		if (null != andNodes) 
		{
			if (andNodes.size() >= 2) 
			{
				key.setType("OR");
				for (Atom node : andNodes) 
				{
					node.setType("AND");
					node.AddParent(key);
					key.AddChild(node);
				}
			}
			
			else 
			{
				key.setType("AND");
			}
			
			this.circuit.lastElement().put(key.toString(), key);
			this.mostRecentOrNodes.put(key.toString(), key);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: AddToCircuit																				
	/** Description: Adds bodies to prov. if it isn't already there			
	/*************************************************************************************************************/
	
	public void AddNodeToCircuit (Atom key, HashSet<Body> bodies, Map<Atom, HashSet<Body>> provenance, List<Atom> queue, boolean isBottomUp, int layerNum)
	{
		if (null != bodies) 
		{
			if (bodies.size() >= 2) 
			{
				key.setType("OR");
				for (Body body : bodies) 
				{					
					Atom curAndNode = new Atom(key);
					curAndNode.setType("AND");
					curAndNode.AddParent(key);
					key.AddChild(curAndNode);
					//curAndNode.setRuleUsed(body.getRuleUsed());
					if (true == isBottomUp)
					{
						HandleAndNodeBottomUp(curAndNode, body);
					}
					
					else
					{
						HandleAndNodeTopDown(curAndNode, body, provenance, queue);
					}
				}
			}
			
			else 
			{
				key.setType("AND");
				if (true == isBottomUp)
				{
					HandleAndNodeBottomUp(key, bodies.iterator().next());// bodies.firstElement()
				}
				
				else
				{
					HandleAndNodeTopDown(key, bodies.iterator().next(), provenance, queue);// bodies.firstElement()
				}
			}
			
			if (true == isBottomUp)
			{
				this.circuit.lastElement().put(key.toString(), key);
				this.mostRecentOrNodes.put(key.toString(), key);
			}
		}		
	}
	
	
	/*************************************************************************************************************/
	/** Title: HandleAndNodeBottomUp																				
	/** Description: Find the last appearance of each atom in the derivation and set this atom as the child of the 
	/** andNode, and set the andNode as the parent of this atom.
	/*************************************************************************************************************/
	
	public void HandleAndNodeBottomUp (Atom andNode, Body body)
	{
		andNode.setRuleUsed(body.getRuleUsed());
		for (Atom bodyAtom : body.getAtoms()) 
		{
			if (true == bodyAtom.isFact()) 
			{
				if (false == this.circuit.firstElement().containsKey(bodyAtom.toString())) 
				{
					this.circuit.firstElement().put(bodyAtom.toString(), bodyAtom);
				}
				
				bodyAtom = this.circuit.firstElement().get(bodyAtom.toString());
				bodyAtom.AddParent(andNode);
				andNode.AddChild(bodyAtom);
			}
			
			else
			{
				Atom origBodyAtom = this.mostRecentOrNodes.get(bodyAtom.toString());
				try 
				{
					origBodyAtom.AddParent(andNode);
				}
				catch (NullPointerException e)
				{
					System.out.println("Circuit::HandleAndNodeBottomUp:: bodyAtom " + bodyAtom + " does not apear in mostRecentOrNodes");
				}
				if (null == andNode.getChildren() || false == andNode.getChildren().contains(bodyAtom)) 
				{
					andNode.AddChild(origBodyAtom);
				}
			}
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: HandleAndNodeTopDown																				
	/** Description: Attaches the andNode's pointers to it's children and vice versa
	/*************************************************************************************************************/
	
	public void HandleAndNodeTopDown (Atom andNode, Body body, Map<Atom, HashSet<Body>> provenance, List<Atom> queue)
	{
		for (Atom bodyAtom : body.getAtoms()) 
		{
			if (bodyAtom.isFact()) 
			{
				bodyAtom.AddParent(andNode);
				andNode.AddChild(bodyAtom);
				Enqueue(queue, bodyAtom); //add to queue
			}
			else
			{
				Atom temp = new Atom (bodyAtom);
				temp.AddParent(andNode);
				if (false == andNode.getChildren().contains(temp)) 
				{
					andNode.AddChild(temp);
				}

				Enqueue(queue, temp);
			}
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: Enqueue																				
	/** Description: Insert element to queue if it is not already in circuit
	/*************************************************************************************************************/
	
	public void Enqueue (List<Atom> queue, Atom atom)
	{
		boolean needToEnqueue = true;
		for (Atom existingAtom : queue) 
		{
			if (existingAtom.equals(atom)) 
			{
				needToEnqueue = false;
				break;
			}
		}
		
		if (true == needToEnqueue) 
		{
			queue.add(atom);
		}
	}
	
	
	/*************************************************************************************************************/
	/** Title: SanityCheck																				
	/** Description: 			
	/*************************************************************************************************************/
	
	public void SanityCheck ()
	{
		for (HashMap<String, Atom> map : this.circuit) 
		{
			for (Atom atom : map.values()) 
			{
				if ((null == atom.getChildren() || atom.getChildren().isEmpty()) && false == atom.isFact()) 
				{
					System.out.println(String.format("SanityCheck Circuit:: atom %s has no children and ins't a fact", atom.toString()));
				}
				
				if (false == atom.isFact() && null == atom.getType()) 
				{
					System.out.println(String.format("SanityCheck Circuit:: atom %s is not a fact and has no type", atom.toString()));
				}
				
				/*if (atom.toString().equals("dealsWith(Andorra,Canada)")) 
				{
					System.out.println("children in level " + this.circuit.indexOf(map) + " are" + atom.getChildren());
				}*/
			}
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: IsRelevantForCircuit																				
	/** Description: Prevent looping			
	/*************************************************************************************************************/
	
	/*public boolean BodyIsOnlyFacts(Vector<Atom> body)
	{
		boolean retVal = true;
		for (Atom atom : body) 
		{
			if (false == atom.isFact()) 
			{
				retVal = false;
			}
		}
		
		return retVal;
	}*/
	
	
	/*************************************************************************************************************/
	/** Title: Size																				
	/** Description: Get size of circuit			
	/*************************************************************************************************************/
	
	public int Size ()
	{
		int size = 0;
		for (HashMap<String, Atom> map : this.circuit) 
		{
			size += map.values().size();
		}
		
		return size;
	}
}
