
from icecream import ic
from conversation import Conversation


def query_gpt():
    convo = Conversation(memory=False)

    prompts = ["my name is katie", "what is my name?"]

    for prompt in prompts:
        response = convo.prompt(prompt)
        print(f"Prompt:{prompt}")
        print(f"Response:{response}")


    responses = convo.responses()
    print(responses)
    print(convo.to_list())


def main():
    with open("answers.txt") as file:
        contents = file.read()

    questions = contents.split("\n\n")
    ic(questions)



if __name__ == "__main__":
    main()

