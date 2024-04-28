
from openai import OpenAI
from constants import API_KEY


class Conversation:
    def __init__(self, model="gpt-3.5-turbo", memory=True):
        self._log = []
        self._model = model
        self._client = OpenAI(api_key=API_KEY)
        self._memory = memory

    def prompt(self, prompt):
        next_message = {"role": "user", "content": prompt}
        self._log.append(next_message)
        current_message_history = self._log if self._memory else [next_message]

        chat_completion = self._client.chat.completions.create(
            model=self._model,
            messages=current_message_history
        )

        response = chat_completion.choices[0].message.content
        self._log.append({"role": "assistant", "content": response})
        return response

    def responses(self):
        return [
            entry["content"]
            for entry in self._log
            if entry["role"] == "assistant"
        ]

    def to_list(self):
        convo = []

        for i in range(0, len(self._log), 2):
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

