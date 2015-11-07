import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Perabjoth Singh Bajwa Fall 2015 CSCI 4401
 *      ******
 *    **********
 *   *************
 *  ***************
 *  **   *****  ***
 *  ***************
 *   ****** ******
 *    ***********
 *     *********
 *    ***********
 *   *************
 *   Beware of Code
 */
public class Deadlock {
    public static void main(String[] args) {
        try {
            //check if input file is provided
            if (args.length > 0) {
                BufferedReader br = new BufferedReader(new FileReader(args[0]));
                RAG rag = new RAG();
                String sCurrentLine;
                ArrayList<String[]> pr = new ArrayList<>();
                while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {//read items from file
                    String[] items = sCurrentLine.split(" ");
                    pr.add(items);
                }
                Locker deadlock = new Locker(false, new ArrayList<String[]>());
                for (int i = 0; i < pr.size(); i++) {//release or allocate resource
                    String[] x = pr.get(i);
                    if (x[1].equals("N"))//alllocate
                        rag.insert(x[0], x[2]);
                    if (x[1].equals("R"))//release
                        rag.free(x[0], x[2]);

                    for (int f = 0; f < rag.elements.size(); f++) {
                        deadlock = rag.checkDeadlock(rag.elements.get(f));//check for deadlock
                        if (deadlock.b) {//when deadlock is detected print out necessary information
                            System.out.print("DEADLOCK DETECTED: Processes ");
                            ArrayList<String> processes = new ArrayList<>();
                            ArrayList<String> resources = new ArrayList<>();
                            for (int j = 0; j < deadlock.s.size(); j++) {
                                if (deadlock.s.get(j)[0].equals("R")) {
                                    resources.add(deadlock.s.get(j)[1]);
                                } else if (deadlock.s.get(j)[0].equals("P")) {
                                    processes.add(deadlock.s.get(j)[1]);
                                }
                            }
                            Collections.sort(processes);
                            Collections.sort(resources);
                            String procs = "";
                            for(int p=0;p<processes.size();p++){
                                procs+=processes.get(p)+",";
                            }
                            String res = "";
                            for(int p=0;p<resources.size();p++){
                                res+=resources.get(p)+",";
                            }
                            System.out.println(procs + " and Resources " + res + " are found in a cycle");
                            break;
                        }
                    }
                    if (deadlock.b) {//stop allocating if deadlock is detected
                        return;
                    }
                }
                if (!deadlock.b) {//if everything is done, then no deadlock is printed
                    System.out.print("EXECUTION COMPLETED: No deadlock encountered");
                }


            } else {
                System.out.println("Program exitting. No input file provided");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Locker {//locker class which has a boolean indicating deadlock as well as array of elements involved
    ArrayList<String[]> s;
    boolean b;

    public Locker(boolean b, ArrayList<String[]> s) {
        this.b = b;
        this.s = s;
    }
}

class RAG {//Resource allocation graph that does most of the work
    ArrayList<Element> elements = new ArrayList<>();//Array of elements
    Resources resources = new Resources();//the resources that are available

    public void insert(String process, String resource) {//allocation of resource to process based on availability

        if (!resources.contains(resource)) {//if resource exists, don't add it
            resources.addResource(resource);
        }
        Resource r = resources.getResource(resource);
        Element e = new Element(process, r);
        if (!r.aquired) {//if resource is free, allocate it to process
            e.waiting = false;
            r.aquire();
            e.resource.aquire();
        }
        elements.add(e);//add to list of elements and then print necessary statements
        System.out.print("Process " + e.process + " needs resource " + e.resource.getName());
        if (!e.waiting) {
            System.out.println(" - Resource " + e.resource.getName() + " is allocated to process " + e.process + ".");
        } else {
            System.out.println(" - Process " + e.process + " must wait.");
        }
    }

    public Locker checkDeadlock(Element e) {//check deadlock for this element
        ArrayList<String[]> deadlock = new ArrayList<>();

        /**
         * make list of processes and resources based on state of element
         * if process is waiting on resource, then get process and then resource and to deadlock list
         * if process is not waiting on resource, then get resource and then process and to deadlock list
         * afterwards
         * if process is waiting on resource, then get process and to deadlock list
         * if process is not waiting on resource, then get resource and to deadlock list
         */

        String r = e.resource.getName();
        String p = e.process;
        if (!e.waiting) {
            String[] e1 = {"P", p};
            String[] e2 = {"R", r};
            deadlock.add(e1);
            deadlock.add(e2);
        } else {
            String[] e1 = {"R", r};
            String[] e2 = {"P", p};
            deadlock.add(e1);
            deadlock.add(e2);
        }
        while (true) {
            r = e.resource.getName();
            p = e.process;

            if (Arrays.deepToString(deadlock.get(0)).equals(Arrays.deepToString(deadlock.get(deadlock.size() - 1))) && deadlock.size() > 3) {
                deadlock.remove(deadlock.size()-1);
                return new Locker(true, deadlock);
            }
            if (!e.waiting) {
                String tempp = e.process;
                String tempr = e.resource.getName();
                for (int i = 0; i < elements.size(); i++) {
                    Element current = elements.get(i);
                    if (!current.process.equals(p) && current.resource.getName().equals(r) && current.waiting) {
                        e = current;
                        String ep = e.process;
                        String er = e.resource.getName();
                        String[][] q = {{"P", ep}, {"R", er}};
                        deadlock.add(q[0]);
                        break;
                    }
                }
                if (e.process.equals(tempp) && e.resource.getName().equals(tempr)) {
                    return new Locker(false, deadlock);
                }
            } else {
                String tempp = e.process;
                String tempr = e.resource.getName();
                for (int i = 0; i < elements.size(); i++) {
                    Element current = elements.get(i);
                    if (current.process.equals(p) && !current.resource.getName().equals(r) && !current.waiting) {
                        e = current;
                        String ep = e.process;
                        String er = e.resource.getName();
                        String[][] q = {{"R", er}, {"P", ep}};
                        deadlock.add(q[0]);
                        break;
                    }
                }
                if (e.process.equals(tempp) && e.resource.getName().equals(tempr)) {
                    return new Locker(false, deadlock);
                }
            }
        }
    }

    /**
     * check if resource is allocated, then free it
     * and then check if any other process needs the resource
     */
    public void free(String process, String resource) {

        for (int i = 0; i < elements.size(); i++) {
            Element current = elements.get(i);
            if (current.process.equals(process) && current.resource.getName().equals(resource)) {
                elements.remove(current);
                System.out.print("Process " + process + " releases resource " + resource + " - ");
                if (!current.waiting) {
                    resources.getResource(resource).release();
                    checkAllocation(resource);
                }
            }
        }
    }

    /**
     * iterate through processes and check if it needs the resource provided
     * as parameter and then print correspondingly
     */
    public void checkAllocation(String resource) {
        boolean aquired = false;
        for (int i = 0; i < elements.size(); i++) {
            Element current = elements.get(i);
            if (current.resource.getName().equals(resource)) {
                if (current.waiting && !resources.getResource(resource).aquired) {
                    current.waiting = false;
                    resources.getResource(resource).aquire();
                    System.out.println("Resource " + resource + " is allocated to process " + current.process);
                    aquired = true;
                }
            }
        }
        if (!aquired) {
            System.out.println("Resource " + resource + " is now free.");
        }
    }
}

class Resources {//Resources class which holds all resources to retrieve allocate and free
    ArrayList<Resource> resources = new ArrayList<Resource>();

    public void addResource(String resource) {
        resources.add(new Resource(resource));
    }


    public boolean contains(String resource) {
        boolean present = false;
        for (Resource r : resources) {
            if (r.getName().equals(resource)) {
                present = true;
            }
        }
        return present;
    }

    public Resource getResource(String resource) {
        Resource resource1 = null;
        for (Resource r : resources) {
            if (r.getName().equals(resource)) {
                resource1 = r;
            }
        }
        return resource1;
    }

}

class Resource {//resource class which is a single resource containing its name and if it is aquired or not
    String resource;
    boolean aquired;

    public Resource(String resource) {
        this.resource = resource;
        this.aquired = false;
    }

    public void aquire() {
        this.aquired = true;
    }

    public void release() {
        this.aquired = false;
    }

    public String getName() {
        return this.resource;
    }
}

class Element {//an element consists of a process and a resource and its current state
    //whether it's waiting on the resource or if it has already been allocated
    String process;
    Resource resource;
    boolean waiting;

    public Element(String process, Resource resource) {
        this.process = process;
        this.resource = resource;
        this.waiting = true;
    }
}