class User:
    def __init__(self, id: int, name: str, address: str):
        self.id = id
        self.name = name
        self.address = address

    def __str__(self):
        return ", ".join((f"{item}: {self.__dict__[item]}" for item in self.__dict__))
