package com.example.mts.model.service

import com.example.mts.model.dao.AccountDAO
import com.example.mts.model.entity.Account

import scala.concurrent.{ExecutionContext, Future}

trait AccountService {

  import AccountService._

  /** Retrieve an account from the id. */
  def findById(id: String): Future[Option[Account]]

  /** Retrieve an account from the id. */
  def list(offset: Int, limit: Int): Future[Seq[Account]]

  /** create new account */
  def create(account: Account): Future[Int]

  /**
    * Performs money transfer from `idFrom` account to `idTo` account
    *
    * @param idFrom    id of account from which we are transfering money
    * @param idTo      id of account to which we are transfering money
    * @param amount    amount of money
    * @param timestamp timestamp of operation
    * @throws IllegalStateException  when invariant checks against db is failed
    * @throws NoSuchElementException when one of elements not found
    * @return
    */
  def transferMoney(idFrom: String, idTo: String, amount: BigDecimal, timestamp: Long): Future[MoneyTransferResult]
}

object AccountService {

  /** result of [[AccountService.transferMoney]] method */
  sealed trait MoneyTransferResult

  case class MoneyTransferError(msg: String) extends MoneyTransferResult

  case object MoneyTransferSuccess extends MoneyTransferResult

}

final class AccountServiceImpl(dao: AccountDAO)(implicit ec: ExecutionContext) extends AccountService {

  import AccountService._

  /** Retrieve an account from the id. */
  def findById(id: String): Future[Option[Account]] = {
    dao.findById(id)
  }

  /** Retrieve an account from the id. */
  def list(offset: Int, limit: Int): Future[Seq[Account]] = {
    dao.list(offset = offset, limit = limit)
  }

  /** create new account */
  def create(account: Account): Future[Int] = {
    dao.create(account)
  }

  /**
    *
    * @param idFrom    id of account from which we are transfering money
    * @param idTo      id of account to which we are transfering money
    * @param amount    amount of money
    * @param timestamp timestamp of operation
    * @return
    */
  def transferMoney(
    idFrom: String,
    idTo: String,
    amount: BigDecimal,
    timestamp: Long
  ): Future[MoneyTransferResult] = {
    dao.transferMoney(idFrom = idFrom, idTo = idTo, amount = amount, timestamp = timestamp)
      .recover {
        case ex: IllegalStateException =>
          MoneyTransferError(ex.getMessage)

        case ex: NoSuchElementException =>
          MoneyTransferError(ex.getMessage)
      }.map(_ => MoneyTransferSuccess)
  }
}