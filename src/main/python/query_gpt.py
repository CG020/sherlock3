
from conversation import Conversation


CONVO = Conversation(memory=False)
PROMPT_HEADER = ("Given the following query and documents, "
                 "give me the title only of the correct answer. "
                 "Select your answer from only the documents listed.")


def read_file(filename):
    with open(filename) as file:
        contents = file.read()

    answers = (q.strip()
               for q in contents.split("\n\n\n")
               if q.startswith("Query"))

    for answer in answers:

        # remove the last line with the original answer
        correct_answer = answer[answer.rindex("\n"):].strip()
        answer = answer[:answer.rindex("\n")]
        query, *documents = answer.split("\n")

        # remove the word "returned" at the end
        query = query[:-len(" returned:")]

        # remove scores from each document
        documents = [doc[:doc.index(":")].strip() for doc in documents]

        yield query, "\n".join(documents), correct_answer


def main():
    with open("GPT_answers.txt", "w") as file:
        queries = []
        correct_answers = []
        for query, documents, correct_answer in read_file("answers.txt"):
            print(query)
            queries.append(query)
            correct_answers.append(correct_answer)
            response = CONVO.prompt(f"{PROMPT_HEADER}\n{query}\n{documents}")
            file.write(query + "\n")
            file.write(f"\t{'\n\t'.join(documents.split('\n'))}\n")
            file.write(response + "\n\n\n")

        num_correct = 0
        for query, response, correct_answer\
                in zip(queries, CONVO.responses(), correct_answers):
            is_correct_response = response in correct_answer.split("|")
            num_correct += is_correct_response

        file.write(f"\n\nFINAL COUNT: {num_correct}\n")

if __name__ == "__main__":
    main()

