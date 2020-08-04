package phonebook;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;

class Person {
    private final String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

class Entry {
    private final Person person;
    private final int phoneNumber;

    public Entry(Person person, int phoneNumber) {
        this.person = person;
        this.phoneNumber = phoneNumber;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public String getName() {
        return person.getName();
    }

    public int compareName(Entry entry) {
        return person.getName().compareTo(entry.getName());
    }
}

class Directory {

    private List<Entry> entries = new ArrayList<Entry>();
    private boolean sorted = false;

    public Directory(String fileName) {

        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNext()) {
                int nextNumber = scanner.nextInt();
                String name = scanner.nextLine().trim();
                entries.add(new Entry(new Person(name), nextNumber));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
        }
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public boolean isSorted() {
        return sorted;
    }

    public void setSorted(boolean val) {
        sorted = val;
    }

    public void sortDirectory(SortMethod method, long allowedTime) {
        if (method != null) {
            method.sort(this, allowedTime);
        }

        // save to file
        if (sorted) {
            // System.out.println("Writing to file");
            File sortedFile = new File("sortedDirectory.txt");
            try (FileWriter writer = new FileWriter(sortedFile)) {
                for (int i = 0; i < entries.size(); i++) {
                    writer.write(entries.get(i).getPhoneNumber() + " " +
                            entries.get(i).getName() + "\n");
                }
            } catch (IOException e) {
                System.out.println("Problem saving sorted directory.");
            }
        }
    }
    public void swap(int i, int j) {
        Collections.swap(entries, i, j);
    }

    public String nameAtIndex(int index) {
        return entries.get(index).getName();
    }
}

interface SearchMethod {
    boolean isListed(Directory directory, Person person);
    String methodName();
}

class BinarySearch implements SearchMethod {

    @Override
    public boolean isListed(Directory directory, Person person) {

        List<Entry> list = directory.getEntries();
        return binarySearch(list, person, 0, list.size() - 1) >= 0;
    }

    private int binarySearch(List<Entry> list, Person person, int left, int right) {

        if (left > right) { // searched without finding
            return -1;
        }

        int mid = left + (right - left) / 2; // middle
        int compare = list.get(mid).getName().compareTo(person.getName());
        if (compare == 0) {
            return mid;
        } else if (compare < 0) { // search right
            return binarySearch(list, person, mid + 1, right);
        } else { // search left
            return binarySearch(list, person, left, mid - 1);
        }
    }

    @Override
    public String methodName() { return "quick sort + binary search";}
}

class LinearSearch implements SearchMethod {

    @Override
    public boolean isListed(Directory directory, Person person) {
        for (Entry entry : directory.getEntries()) {
            if (person.getName().equals(entry.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String methodName() {
        return "linear search";
    }
}

class JumpSearch implements SearchMethod {

    @Override
    public boolean isListed(Directory directory, Person person) {
        int prevRight = 0;
        int currentRight = 0;

        List<Entry> list = directory.getEntries();

        if (list.size() == 0) {
            return false;
        }

        if (list.get(0).getName().equals(person.getName())) {
            return true;
        }

        int jumpLength = (int) Math.sqrt(list.size());

        while (currentRight < list.size() - 1) {
            currentRight = Math.min(list.size() - 1, currentRight + jumpLength);

            if (list.get(currentRight).getName().compareTo(person.getName()) >= 0) {
                break; // possible block found
            }

            prevRight = currentRight;
        }

        if ((currentRight == list.size() - 1) &&
                person.getName().compareTo(list.get(currentRight).getName()) > 0) {
            return false; // beyond scope
        }

        return backwardSearch(list, person, prevRight, currentRight);
    }

    public static boolean backwardSearch(List<Entry> list, Person person,
                                         int leftExcl, int rightIncl) {
        for (int i = rightIncl; i > leftExcl; i--) {
            if (list.get(i).getName().equals(person.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String methodName() {
        return "bubble sort + jump search";
    }
}

abstract class SortMethod {
    abstract void sort(Directory directory, long allowedTime);
}

class QuickSort extends SortMethod {

    public void sort(Directory directory, long allowedTime) {
        List<Entry> list = directory.getEntries();


        quickSort(directory, 0, list.size() - 1);

        directory.setSorted(true);
    }

    private void quickSort(Directory directory, int left, int right) {
        // choose a pivot element (rightmost)
        // reorder array with smaller values on left
        // recursively sort the subarrays
        if (left < right) {
            int pivotIndex = partition(directory, left, right);
            quickSort(directory, left, pivotIndex - 1);
            quickSort(directory, pivotIndex + 1, right);
        }
    }

    private static int partition(Directory directory, int left, int right) {
        List<Entry> list = directory.getEntries();
        String pivot = list.get(right).getName();
        int partitionIndex = left;

        // swap smaller with larger values
        for (int i = left; i < right; i++) {
            if (list.get(i).getName().compareTo(pivot) < 0) {
                directory.swap(i, partitionIndex);
                partitionIndex++;
            }
        }

        directory.swap(right, partitionIndex);

        return partitionIndex;
    }
}

class BubbleSort extends SortMethod {

    public void sort(Directory directory, long allowedTime) {
        List<Entry> list = directory.getEntries();

        long startTime = System.currentTimeMillis();
        long runTime = startTime;
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - i - 1; j++) {
                if (list.get(j).compareName(list.get(j + 1)) > 0) {
                    Entry temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }

                runTime = System.currentTimeMillis();
                if (runTime - startTime > allowedTime) { // taking too long
                    return;
                }
            }
        }
        directory.setSorted(true);
    }
}

enum SearchType {LINEAR, JUMP, BINARY}
enum SortType {UNSORTED, BUBBLE, QUICK}

class SearchManager {
    private SearchMethod searchMethod;
    private SearchType searchType;
    private long maxAllowedTime;

    public long getMaxAllowedTime() {
        return maxAllowedTime;
    }

    public void setMaxAllowedTime(long t) {
        maxAllowedTime = t >= 0 ? t : 0;
    }
    public void setSearchMethod(SearchType method) {
        searchType = method;
        switch (method) {
            case LINEAR:
                searchMethod = new LinearSearch();
                break;
            case JUMP:
                searchMethod = new JumpSearch();
                break;
            case BINARY:
                searchMethod = new BinarySearch();
                break;
            default:
                break;
        }
    }


    public void runListSearch(Directory directory, List<Person> people) {

        if (searchType == null) {
            System.out.println("Error: Search algorithm not set.");
            return;
        }

        int countTried = 0;
        int countFound = 0;

        System.out.println("\nStart searching (" + searchMethod.methodName() + ")...");

        boolean sortCancelled = false;
        long sortTimeStart = -1;
        long sortTimeEnd = -1;

        if (searchType != SearchType.LINEAR) {
            // sort directory
            sortTimeStart = System.currentTimeMillis();
            SortMethod sortMethod = null;
            if (searchType == SearchType.JUMP) {
                sortMethod = new BubbleSort();
            } else if (searchType == SearchType.BINARY) {
                sortMethod = new QuickSort();
            }

            directory.sortDirectory(sortMethod, maxAllowedTime);

            // record completion time
            sortTimeEnd = System.currentTimeMillis();

            if (!directory.isSorted()) {
                // stop and do a linear search
                setSearchMethod(SearchType.LINEAR);
                sortCancelled = true;
            }
        }

        long searchTimeStart = System.currentTimeMillis();
        for (Person person : people) {
            // search directory for person
            // if name is found in directory, increment count
            countTried++;
            if (searchMethod.isListed(directory, person)) {
                countFound++;
            }
        }

        long searchTimeEnd = System.currentTimeMillis();
        String timeTaken = "Time taken: ";
        if (sortTimeStart > 0) {
            timeTaken += timeTakenString(sortTimeEnd - sortTimeStart
                    + searchTimeEnd - searchTimeStart);
        } else {
            if (searchType == SearchType.LINEAR) {
                setMaxAllowedTime(10 * (searchTimeEnd - searchTimeStart));
            }
            timeTaken += timeTakenString(searchTimeEnd - searchTimeStart);
        }

        System.out.printf("Found %d / %d entries. %s\n", countFound, countTried,
                timeTaken);

        if (sortTimeStart > 0) {
            String sortTimeTaken = timeTakenString(sortTimeEnd - sortTimeStart);
            if (sortCancelled) {
                sortTimeTaken += " - STOPPED, moved to linear search";
            }
            System.out.printf("Sorting time: %s\n", sortTimeTaken);
            System.out.printf("Searching time: %s\n", timeTakenString(searchTimeEnd - searchTimeStart));
        }
    }

    private String timeTakenString(long time) {
        long min = time / 60_000;
        long seconds = (time % 60_000) / 1000;
        long milliseconds = time % 1000;

        return min + " min. " + seconds + " sec. "
                + milliseconds + " ms.";
    }
}

public class Main {
    public static void main(String[] args) {

        // import directory
        String directoryPath = "C:\\Users\\Cmcm8\\IdeaProjects\\directory.txt";
        Directory directory = new Directory(directoryPath);

        // import persons to search for
        String findFilePath = "C:\\Users\\Cmcm8\\IdeaProjects\\find.txt";
        List<Person> people = getPeopleFromFile(findFilePath);

        // Initialize a SearchManager and try a linear search for 'people'
        SearchManager searchManager = new SearchManager();
        searchManager.setSearchMethod(SearchType.LINEAR);
        searchManager.runListSearch(directory, people);

        // try a jump search
        searchManager.setSearchMethod(SearchType.JUMP);
        searchManager.runListSearch(directory, people);

        // Create a fresh unsorted directory to observe sort and search times
        Directory directoryTwo = new Directory(directoryPath);

        // try binary search with new directory
        searchManager.setSearchMethod(SearchType.BINARY);
        searchManager.runListSearch(directoryTwo, people);

    }

    public static List<Person> getPeopleFromFile(String filePath) {
        List<Person> people = new ArrayList<Person>();

        try (Scanner scanner = new Scanner(new File(filePath))) {

            while (scanner.hasNext()) {
                String name = scanner.nextLine().trim();
                people.add(new Person(name));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found: " + filePath);
        }

        return people;

    }
}
