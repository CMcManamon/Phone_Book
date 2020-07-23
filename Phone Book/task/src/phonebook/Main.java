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

    public void sortDirectory(SortType method) {
        if (method == SortType.BUBBLE) {
            BubbleSort.sort(entries);
        }

        // save to file
        if (sorted) {
            System.out.println("Writing to file");
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

        return true;
    }

    @Override
    public String methodName() {
        return "bubble sort + jump search";
    }
}

abstract class BubbleSort {

    public static void sort(List<Entry> list) {
        long count = 0;
        for (int i = 0; i < list.size() - 1; i++) {

            for (int j = 0; j < list.size() - i - 1; j++) {
                if (list.get(j).compareName(list.get(j + 1)) > 0) {
                    count++;
            //        Entry temp = list.get(j);
             //       list.set(j, list.get(j + 1));
               //     list.set(j + 1, temp);
                }
            }
            if (i <= 1) {
                System.out.println(count + " of " + list.size());
                count = 0;
            }

        }
//        directory.setSorted(true);
    }
}

enum SearchType {LINEAR, JUMP}
enum SortType {BUBBLE}

class SearchManager {
    private SearchMethod method;
    private SearchType searchType;

    public void setSearchMethod(SearchType method) {
        searchType = method;
        switch (method) {
            case LINEAR:
                this.method = new LinearSearch();
                break;
            case JUMP:
                this.method = new JumpSearch();
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

        System.out.println("\nStart searching (" + method.methodName() + ")...");

        boolean sortCancelled = false;
        long sortTimeStart = -1;
        long sortTimeEnd = -1;

        if (searchType == SearchType.JUMP) {
            // sort directory
            sortTimeStart = System.currentTimeMillis();
            directory.sortDirectory(SortType.BUBBLE);

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
            if (method.isListed(directory, person)) {
                countFound++;
            }
        }

        long searchTimeEnd = System.currentTimeMillis();
        String timeTaken = "Time taken: ";
        if (sortTimeStart > 0) {
            timeTaken += timeTakenString(sortTimeEnd - sortTimeStart
                    + searchTimeEnd - searchTimeStart);
        } else {
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
            System.out.printf("Searching time: %s", timeTakenString(searchTimeEnd - searchTimeStart));
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

        SearchManager searchManager = new SearchManager();
        searchManager.setSearchMethod(SearchType.LINEAR);
        searchManager.runListSearch(directory, people);

        searchManager.setSearchMethod(SearchType.JUMP);
        searchManager.runListSearch(directory, people);
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
