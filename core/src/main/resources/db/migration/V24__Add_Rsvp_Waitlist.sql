ALTER TABLE rsvp
    ADD COLUMN waitlist LONGTEXT not null
        after UNDECIDED;
