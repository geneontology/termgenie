% The possible suspects of the crimes are:

possible_suspect(fred).
possible_suspect(mary).
possible_suspect(jane).
possible_suspect(george).

% The facts about the crimes from the police log.

crime(robbery1, john, tuesday, park).
crime(assault1, mary, wednesday, park).
crime(robbery2, jim, wednesday, pub).
crime(assault2, robin, thursday, park).

% Tell prolog where the suspects were on
% different days.

was_at(fred, park, tuesday).
was_at(fred, pub, wednesday).
was_at(fred, pub, thursday).

was_at(george, pub, tuesday).
was_at(george, pub, wednesday).
was_at(george, home, thursday).

was_at(jane, home, tuesday).
was_at(jane, park, wednesday).
was_at(jane, park, thursday).

was_at(mary, pub, tuesday).
was_at(mary, park, wednesday).
was_at(mary, home, thursday).

% Tell prolog who is jealous of who

jealous_of(fred, john).
jealous_of(jane, mary).

% And who owes money to whom

owes_money_to(george, jim).
owes_money_to(mary, robin).

% A Person has a motive against a Victim if
% Person is jealous of Victim or
% Person owes money to Victim

motive_against(Person, Victim) :-
    jealous_of(Person, Victim).

motive_against(Person, Victim) :-
    owes_money_to(Person, Victim).

% A Person is a prime suspect of a crime if
% Person is a possible suspect and the person
% was at the time and place of the crime and
% the person had a motive against the victim
% of the crime.

prime_suspect(Person, Crime) :-
    possible_suspect(Person),
    was_at(Person, Place, Day),
    crime(Crime, Victim, Day, Place),
    motive_against(Person, Victim).
