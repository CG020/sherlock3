
from icecream import ic
import json
import openai
import pandas as pd
from ai_project.constants import API_KEY

openai.api_key = API_KEY

class Conversation:
    def __init__(self, model="gpt-3.5-turbo-16k"):
        self._log = []
        self._model = model


    def prompt(self, prompt):
        self._log.append({"role": "user", "content": prompt})

        chat_completion = openai.ChatCompletion.create(
            model=self.model,
            messages=self._log,
        )

        response = chat_completion["choices"][0]["message"]["content"]
        self._log.append({"role": "assistant", "content": response})
        return response


    def to_list(self):
        convo = []

        for i in range(0, self._log, 2):
            user_message = self._log[i]
            robot_message = self._log[i+1]

            assert user_message['role'] == "user"

            prompt = user_message["content"]
            response = robot_message["content"]

            row = {
                "prompt": prompt,
                "response": response
            }

            convo.append(row)

        return convo


def main():
    convo = Conversation()

    response = convo.prompt("hey, how are you doing?")
    ic(response)

    response = convo.prompt("I have a question about xyz")
    ic(response)

    responses = convo.responses()
    ic(responses)
    ic(convo.to_list())


if __name__ == '__main__':
    main()
