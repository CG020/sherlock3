"""
Katelyn Rohrer, Lydia Dufek, Camila Grubb
CSC 483/583
This program writes the class to manage and query the ChatGPT API.
By default, GPT 3.5 turbo is used, and conversation history is stored in
the query to the API. To run this program, a file in the same directory
titled 'constants.py' must be defined with the constant API_KEY defined
within it. The API key is necessary to run this class.
"""


from openai import OpenAI
from constants import API_KEY


class Conversation:
    """
    Stores and manages a conversation with a GPT model.
    By default, GPT 3.5 turbo is used and conversation memory is set to True.
    Noteworthy methods to this class include `prompt`, which sends a prompt
    to the API, and `responses`, which returns a list of all responses within
    the conversation.
    """
    def __init__(self, model="gpt-3.5-turbo", memory=True):
        """
        Initializes the Conversation object.
        Attributes include:
            _log:    List of conversation log up to this point
            _model:  String model name of the GPT
            _client: The OpenAI object which can be used to query GPT
            _memory: Boolean defining whether the model should retain memory
                     of the prior messages when querying
        """
        self._log = []
        self._model = model
        self._client = OpenAI(api_key=API_KEY)
        self._memory = memory

    def prompt(self, message):
        """
        Prompts the GPT API with a string message
        :param message: The string message to be sent to GPT
        :return: The GPT response to the given message
        """
        next_message = {"role": "user", "content": message}
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
        """
        Compiles a list of GPT responses from the message log
        :return: List of strings containing GPT responses
        """
        return [
            entry["content"]
            for entry in self._log
            if entry["role"] == "assistant"
        ]
