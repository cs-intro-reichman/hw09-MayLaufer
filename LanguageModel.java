import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		String window = "";
        char c;

        In in = new In(fileName);

        for (int i = 0; i < this.windowLength; i++) {
            window = window + in.readChar();
        }

        while (!in.isEmpty()) {
            c = in.readChar();

            List probs = new List();

            if (CharDataMap.containsKey(window)) {
                probs = CharDataMap.get(window);
            } else {
                probs = new List();
                CharDataMap.put(window, probs);
            }

            probs.update(c);

            window = window + c;
            window = window.substring(1);

        }

        for (List probs : CharDataMap.values())
                calculateProbabilities(probs);

        
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
		ListIterator itr = probs.listIterator(0);
        int charCount = 0;
        int i = 0;
        
        while (itr.hasNext()) {
            charCount += itr.next().count;
        }

        itr = probs.listIterator(0);

        while (itr.hasNext()) {
            CharData current = itr.next();
            current.p = (double) current.count / charCount;
        }

        itr = probs.listIterator(0);
        double cumulativeProb = 0.0;

        while (itr.hasNext()) {
            CharData current = itr.next();
            current.cp = cumulativeProb + current.p; 
            cumulativeProb += current.cp;
        }

	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble();
        ListIterator itr = probs.listIterator(0);
        int index = 0;
        char chr = ' ';

        while (itr.hasNext()) {

            if (probs.get(index).cp > r) {
                chr = probs.get(index).chr;
                return chr;
            }

            index++;
            itr.next();

        } 

        return chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < windowLength) {
            return initialText;
        }

        String window = initialText.substring(initialText.length() - windowLength);
        String generatedText = " "; 

        while (generatedText.length() < textLength) {
            if (!CharDataMap.containsKey(window)) {
                return generatedText;
            }

            List probs = CharDataMap.get(window);
            char c = getRandomChar(probs);
            generatedText += c;
            window = generatedText.substring(initialText.length() - windowLength);
        }

        return generatedText;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
    }
}
